package com.example.auth.service.Service;

import com.example.auth.service.DTO.AuthenticationRequest;
import com.example.auth.service.DTO.AuthenticationResponse;
import com.example.auth.service.DTO.RegisterRequest;
import com.example.auth.service.Enums.Role;
import com.example.auth.service.Enums.TokenType;
import com.example.auth.service.Model.Token;
import com.example.auth.service.Model.User;
import com.example.auth.service.Repository.TokenRepository;
import com.example.auth.service.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private  Logger logger= Logger.getLogger(this.getClass().getName());

    public AuthenticationResponse register(RegisterRequest request) {
        // التحقق من وجود المستخدم بالفعل
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already registered");
        }
        logger.info("Registering new user" + request.toString());


        String encodedPassword = passwordEncoder.encode(request.getPassword());

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        // Logging the encoded password to check if it's there
        logger.info("Encoded password before saving: " + user.getPassword());

        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse
                .builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        Logger log=Logger.getLogger(this.getClass().getName());
        log.info("Authenticated user: " + request.getUsername());
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserToken(user);
        saveUserToken(user,jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    // هذه هي الـmethod الجديدة التي ستُستخدم من الـGateway
    public String validateTokenAndGetUserInfo(String token) {
        try {
            // 1. استخراج اسم المستخدم من الـtoken
            String userUsername = jwtService.extractUsername(token);

            if (userUsername != null) {
                // 2. تحميل تفاصيل المستخدم من قاعدة البيانات
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userUsername);

                // 3. التحقق من صلاحية الـtoken في قاعدة البيانات (غير منتهٍ أو ملغى)
                var isTokenValid = tokenRepository.findByToken(token)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElse(false);

                // 4. التحقق من صحة توقيع الـtoken
                if (jwtService.isTokenValid(token, userDetails) && isTokenValid) {
                    System.out.println("user data are "+userDetails.getUsername());
                    System.out.println("Authorities are " + userDetails.getAuthorities());
                    return userDetails.getAuthorities().stream()
                            .map(authority -> authority.getAuthority())
                            .collect(Collectors.joining(","));
                }
            }
        } catch (Exception e) {
            // في حالة وجود خطأ (مثل token غير صحيح)، قم بالتعامل معه هنا
            return null; // أو إرجاع قيمة تدل على الفشل
        }
        return null;
    }

    public void revokeAllUserToken(User user){
        var validUserToken = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserToken.isEmpty()) return;
        validUserToken.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserToken);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        final String authHeader= request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userUsername;
        if (authHeader==null || !authHeader.startsWith("Bearer "))
        {
            return;
        }
        refreshToken= authHeader.substring(7);
        userUsername = jwtService.extractUsername(refreshToken);
        if (userUsername !=null){
            var user=this.userRepository.findByUsername(userUsername)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken,user)){
                var accessToken = jwtService.generateToken(user);
                revokeAllUserToken(user);
                saveUserToken(user,accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(),authResponse);
            }
        }
    }
}

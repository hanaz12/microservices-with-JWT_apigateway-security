package com.example.auth.service.Controller;

import com.example.auth.service.DTO.AuthenticationRequest;
import com.example.auth.service.DTO.AuthenticationResponse;
import com.example.auth.service.DTO.RegisterRequest;
import com.example.auth.service.Service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));

    }


    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        Logger log=Logger.getLogger(this.getClass().getName());
        log.info(request.toString());
        return ResponseEntity.ok(authenticationService.authenticate(request));

    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @GetMapping("/validate-and-get-info")
    public ResponseEntity<String> validateAndGetUserInfo(@RequestParam("token") String token) {
        String roles = authenticationService.validateTokenAndGetUserInfo(token);
        if (roles != null) {
            return ResponseEntity.ok(roles);
        } else {
            // حالة فشل التحقق
            return ResponseEntity.status(401).body("Invalid token or no roles found");
        }
    }
}
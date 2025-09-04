package com.example.api_gateway.Filters;

import com.example.api_gateway.Routes.RouteValidator;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component

public class AuthenticationFilterGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationFilterGatewayFilterFactory.Config> {

    private final WebClient.Builder webClientBuilder;
    private final RouteValidator validator;

    public AuthenticationFilterGatewayFilterFactory(WebClient.Builder webClientBuilder, RouteValidator validator) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
        this.validator = validator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

         // check if the endpoint doesn't need security so this will be routed
            if (!validator.isSecured.test(exchange.getRequest())) {
                System.out.println("Endpoint is not secured, bypassing authentication filter.");
                return chain.filter(exchange);
            }

            //we need to check that the headers contains Auttorization key
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                System.err.println("Authorization header is missing.");
                return Mono.error(new RuntimeException("Unauthorized access to application: Missing token"));
            }

           // we need to check that authorization isn't empty and contains token
            String authHeader = Objects.requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
            String token = authHeader.substring(7);
            System.out.println("Authorization filter started for secured endpoint with token: " + token);

            // talking to auth service to validate token
            return webClientBuilder.build().get()
                    .uri("lb://AUTH-SERVICE/api/v1/auth/validate-and-get-info", uriBuilder -> uriBuilder.queryParam("token", token).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(roles -> {

                        System.out.println("Authorization successful. Roles received: " + roles);

                  // Exchande is immutable so we need to build a new one and add headers to it
                        var mutatedExchange = exchange.mutate()
                                .request(r -> r.headers(headers -> headers.add("X-Auth-Roles", roles)))
                                .build();
// we need to continue with our chain but send with it the mutated exchange (the new one with headers)
                        return chain.filter(mutatedExchange);
                    })
                    .onErrorResume(e -> {

                        System.err.println("Error during authentication: " + e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }


    public static class Config {
    }
}

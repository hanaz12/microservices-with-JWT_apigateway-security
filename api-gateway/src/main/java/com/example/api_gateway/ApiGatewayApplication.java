package com.example.api_gateway;

import com.example.api_gateway.Filters.AuthenticationFilterGatewayFilterFactory;
import com.example.api_gateway.Routes.RouteValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.logging.Logger;


@SpringBootApplication

@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
	@Bean
	@LoadBalanced

	public WebClient.Builder getWebClientBuilder() {
		return WebClient.builder();
	}

//	@Bean
//	public AuthenticationFilterGatewayFilterFactory authenticationFilterGatewayFilterFactory(WebClient.Builder webClientBuilder, RouteValidator validator) {
//		Logger logger=Logger.getLogger(ApiGatewayApplication.class.getName());
//		logger.info("********************** filter Bean injectedd*************");
//		return new AuthenticationFilterGatewayFilterFactory(webClientBuilder, validator);
//	}
}

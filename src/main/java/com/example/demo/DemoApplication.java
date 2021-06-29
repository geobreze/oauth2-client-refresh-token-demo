package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public WebClient webClient(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService clientService
    ) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .refreshToken()
                // password since client_credentials auth type doesn't use refresh token
                .password()
                .build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                clientService
        );

        clientManager.setAuthorizedClientProvider(authorizedClientProvider);
        clientManager.setContextAttributesMapper(request ->
                Map.of(
                        OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, "a",
                        OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, "b"
                )
        );

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth.setDefaultClientRegistrationId("test");
        return WebClient.builder()
                .filter(oauth)
                .build();
    }
}

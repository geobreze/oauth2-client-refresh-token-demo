package com.example.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureWireMock(port = 9000)
class DemoApplicationTests {

    @Autowired
    WebClient webClient;

    @Test
    void shouldRefreshTokenInInfiniteLoopEvenWhenRefreshReturnsError() {
        // expired
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjIsImlhdCI6MCwidHlwIjoiQmVhcmVyIiwiYXpwIjoidGVzdCIsInNjb3BlIjoiIn0.-dhakXiubwI2wS24lU00WFzag8-IjaUX6fSzfMN6j2A";
        // good
        String refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjEwMDAwMDAwMDAwLCJpYXQiOjAsInR5cCI6IlJlZnJlc2giLCJhenAiOiJ0ZXN0Iiwic2NvcGUiOiIifQ.JfLz7veCA75DCc--3Hf0Zj1bq9WpSIuANw6CR2rccUg";

        String accessTokenResponseBody = String.format("{\"expires_in\": 1, \"refresh_expires_in\": 30000, \"token_type\": \"bearer\", \"access_token\":\"%s\", \"refresh_token\":\"%s\"}", accessToken, refreshToken);
        stubFor(post("/token").withRequestBody(containing("grant_type=password")).willReturn(aResponse().withBody(accessTokenResponseBody).withStatus(200).withHeader("Content-Type", "application/json")));

        String errorMessage = "{\"error\": \"test\", \"error_description\": \"test desc\"}";
        stubFor(post("/token").withRequestBody(containing("grant_type=refresh_token")).willReturn(aResponse().withBody(errorMessage).withStatus(400).withHeader("Content-Type", "application/json")));

        stubFor(get("/test").willReturn(aResponse()));

        // first request with OAuth2 using password
        webClient.get()
                .uri("http://localhost:9000/test")
                .retrieve()
                .toBodilessEntity()
                .block();

        String msg1 = Assertions.assertThrows(OAuth2AuthorizationException.class, () -> webClient.get()
                .uri("http://localhost:9000/test")
                .retrieve()
                .toBodilessEntity()
                .block()).getError().toString();

        String msg2 = Assertions.assertThrows(OAuth2AuthorizationException.class, () -> webClient.get()
                .uri("http://localhost:9000/test")
                .retrieve()
                .toBodilessEntity()
                .block()).getError().toString();

        // first token refresh throws, but not clears cache for refresh token
        Assertions.assertEquals("[test] test desc", msg1);
        // second token refresh throws same error causing infinite loop of refreshes, until application restarts
        Assertions.assertEquals("[test] test desc", msg2);
    }
}
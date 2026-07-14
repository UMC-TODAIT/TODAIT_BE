package com.example.TODAIT__BE.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@Configuration
public class OAuthClientConfig {
    private static final Duration KAKAO_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration KAKAO_READ_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    public ClientHttpRequestFactory kakaoRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(KAKAO_CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(KAKAO_READ_TIMEOUT);
        return requestFactory;
    }
}
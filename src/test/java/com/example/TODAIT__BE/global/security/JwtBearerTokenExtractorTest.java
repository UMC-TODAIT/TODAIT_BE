package com.example.TODAIT__BE.global.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtBearerTokenExtractorTest {

    @Test
    void extractAcceptsBearerSchemeCaseInsensitively() {
        assertThat(JwtBearerTokenExtractor.extract("Bearer token"))
                .contains("token");
        assertThat(JwtBearerTokenExtractor.extract("bearer token"))
                .contains("token");
        assertThat(JwtBearerTokenExtractor.extract("BEARER token"))
                .contains("token");
        assertThat(JwtBearerTokenExtractor.extract("BeArEr token"))
                .contains("token");
    }

    @Test
    void extractReturnsEmptyWhenAuthorizationIsNull() {
        assertThat(JwtBearerTokenExtractor.extract(null))
                .isEmpty();
    }

    @Test
    void extractReturnsEmptyWhenPrefixIsInvalid() {
        assertThat(JwtBearerTokenExtractor.extract("Basic token"))
                .isEmpty();
    }

    @Test
    void extractReturnsEmptyWhenTokenIsBlank() {
        assertThat(JwtBearerTokenExtractor.extract("Bearer "))
                .isEmpty();
    }
}

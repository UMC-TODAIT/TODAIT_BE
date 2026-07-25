package com.example.TODAIT__BE.global.security;

import java.util.Optional;

public final class JwtBearerTokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    private JwtBearerTokenExtractor() {
    }

    public static Optional<String> extract(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        if (token.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(token);
    }
}

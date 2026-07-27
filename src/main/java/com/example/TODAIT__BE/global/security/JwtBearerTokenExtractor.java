package com.example.TODAIT__BE.global.security;

import java.util.Optional;

public final class JwtBearerTokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    private JwtBearerTokenExtractor() {
    }

    public static Optional<String> extract(String authorization) {
        if (authorization == null || !hasBearerPrefix(authorization)) {
            return Optional.empty();
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        if (token.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(token);
    }

    private static boolean hasBearerPrefix(String authorization) {
        return authorization.regionMatches(
                true,
                0,
                BEARER_PREFIX,
                0,
                BEARER_PREFIX.length()
        );
    }
}

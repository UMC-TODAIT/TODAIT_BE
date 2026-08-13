package com.example.TODAIT__BE.global.security.token;

public enum TokenType {
    ACCESS,
    REFRESH,
    OAUTH_ONBOARDING;

    public static TokenType fromClaim(String value) {
        if (value == null) {
            return null;
        }

        try {
            return TokenType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}

package com.example.TODAIT__BE.domain.member.dto.response;

import lombok.Builder;

public final class AuthResponse {

    private AuthResponse() {
    }

    @Builder
    public record Token(
            String accessToken,
            String refreshToken
    ) {
    }

    @Builder
    public record AccessToken(
            String accessToken
    ) {
    }
}

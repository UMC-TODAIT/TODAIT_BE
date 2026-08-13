package com.example.TODAIT__BE.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public final class AuthResponse {

    private AuthResponse() {
    }

    @Builder
    @Schema(name = "AuthTokenResponse")
    public record Token(
            String accessToken,
            String refreshToken
    ) {
    }

    @Builder
    @Schema(name = "AuthAccessTokenResponse")
    public record AccessToken(
            String accessToken
    ) {
    }
}

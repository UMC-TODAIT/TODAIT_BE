package com.example.TODAIT__BE.domain.member.dto.response;

import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public final class OAuthResponse {

    private OAuthResponse() {
    }

    @Builder
    @Schema(name = "OAuthLoginResponse")
    public record Login(
            String loginStatus,
            String accessToken,
            String refreshToken,
            String onboardingToken,
            String email,
            OAuthProvider provider
    ) {
    }
}

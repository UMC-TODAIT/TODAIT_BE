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
            OAuthProvider provider,

            @Schema(
                    description = "소셜 계정에서 조회하거나 회원 정보에 저장된 프로필 이미지 URL입니다. "
                            + "값이 null이면 Android 앱에서 투데잇 기본 프로필 이미지를 표시합니다.",
                    example = "https://example.com/profile.jpg",
                    nullable = true
            )
            String profileImageUrl
    ) {
    }
}

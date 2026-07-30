package com.example.TODAIT__BE.domain.member.dto.request;

import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class OAuthRequest {

    private OAuthRequest() {
    }

    @Schema(name = "OAuthKakaoAccessTokenRequest")
    public record KakaoAccessToken(
            @NotBlank
            String accessToken
    ) {
    }

    @Schema(name = "OAuthGoogleIdTokenRequest")
    public record GoogleIdToken(
            @NotBlank
            String idToken
    ) {
    }

    @Schema(name = "OAuthOnboardingRequest")
    public record Onboarding(
            @NotBlank
            @Size(min = 2, max = 12)
            @Pattern(
                    regexp = "^[가-힣a-zA-Z0-9]+$",
                    message = "닉네임에는 한글, 영문, 숫자만 사용할 수 있습니다."
            )
            String nickname,

            @NotEmpty
            List<@Valid TermAgreementRequest> termAgreements
    ) {
        public Onboarding {
            nickname = MemberInputPolicy.normalizeNickname(nickname);
        }
    }
}

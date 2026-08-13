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
            @Schema(
                    description = "Android 카카오 SDK에서 발급받은 카카오 액세스 토큰",
                    example = "kakao_access_token"
            )
            @NotBlank
            String accessToken
    ) {
    }

    @Schema(name = "OAuthGoogleIdTokenRequest")
    public record GoogleIdToken(
            @Schema(
                    description = "Android 구글 로그인에서 발급받은 ID 토큰",
                    example = "google_id_token"
            )
            @NotBlank
            String idToken
    ) {
    }

    @Schema(name = "OAuthOnboardingRequest")
    public record Onboarding(
            @Schema(
                    description = "서비스에서 사용할 닉네임, 2~12자의 한글·영문·숫자",
                    example = "투데잇"
            )
            @NotBlank
            @Size(min = 2, max = 12)
            @Pattern(
                    regexp = "^[가-힣a-zA-Z0-9]+$",
                    message = "닉네임에는 한글, 영문, 숫자만 사용할 수 있습니다."
            )
            String nickname,

            @Schema(
                    description = "약관 유형별 동의 정보. SERVICE와 PRIVACY는 필수 동의"
            )
            @NotEmpty
            List<@Valid TermAgreementRequest> termAgreements
    ) {
        public Onboarding {
            nickname = MemberInputPolicy.normalizeNickname(nickname);
        }
    }
}

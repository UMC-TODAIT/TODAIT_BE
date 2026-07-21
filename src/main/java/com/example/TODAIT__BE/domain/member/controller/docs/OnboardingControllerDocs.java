package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.OAuthOnboardingRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Onboarding",
        description = "카카오/구글 간편 가입 사용자의 온보딩 API"
)
public interface OnboardingControllerDocs {

    @Operation(
            summary = "소셜 회원가입 완료",
            description = """
                신규 소셜 로그인 사용자의 닉네임과 약관 동의 정보를 검증합니다.
                온보딩이 완료되면 회원, 소셜 계정 연결 정보, 약관 동의 내역을 저장하고
                Access Token과 Refresh Token을 발급합니다.
                """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<AuthTokenResponse.Token>> completeOnboarding(
            @Parameter(hidden = true) String authorization,
            OAuthOnboardingRequest.Complete request
    );
}

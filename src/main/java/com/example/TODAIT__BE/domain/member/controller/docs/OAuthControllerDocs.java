package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.OAuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "MEMBER",
        description = "회원 도메인 API"
)
@SecurityRequirements
public interface OAuthControllerDocs {

    @Operation(
            summary = "[인증] 카카오 로그인",
            description = """
                    Android에서 발급한 카카오 Access Token을 검증하여 로그인합니다.
                    기존 회원이면 서비스 Access Token과 Refresh Token을 발급합니다.
                    신규 회원이면 소셜 회원가입 완료에 사용할 온보딩 토큰을 발급합니다.
                    """
    )
    ResponseEntity<ApiResponse<OAuthResponse.Login>> kakaoLogin(
            OAuthRequest.KakaoAccessToken request
    );

    @Operation(
            summary = "[인증] 구글 로그인",
            description = """
                    Android에서 발급한 Google ID Token을 검증하여 로그인합니다.
                    기존 회원이면 서비스 Access Token과 Refresh Token을 발급합니다.
                    신규 회원이면 소셜 회원가입 완료에 사용할 온보딩 토큰을 발급합니다.
                    """
    )
    ResponseEntity<ApiResponse<OAuthResponse.Login>> googleLogin(
            OAuthRequest.GoogleIdToken request
    );
}

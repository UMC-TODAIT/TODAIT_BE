package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.OAuthLoginRequest;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthLoginResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "OAuth",
        description = "카카오/구글 소셜 로그인 API"
)
@SecurityRequirements
public interface OAuthControllerDocs {

    @Operation(
            summary = "카카오 로그인",
            description = """
                    Android에서 발급한 카카오 Access Token을 검증하여 로그인합니다.
                    기존 회원이면 서비스 Access Token과 Refresh Token을 발급합니다.
                    신규 회원이면 소셜 회원가입 완료에 사용할 온보딩 토큰을 발급합니다.
                    """
    )
    ResponseEntity<ApiResponse<OAuthLoginResponse.OAuthLogin>> kakaoLogin(
            OAuthLoginRequest.KakaoAccessToken request
    );

    @Operation(
            summary = "구글 로그인",
            description = """
                    Android에서 발급한 Google ID Token을 검증하여 로그인합니다.
                    기존 회원이면 서비스 Access Token과 Refresh Token을 발급합니다.
                    신규 회원이면 소셜 회원가입 완료에 사용할 온보딩 토큰을 발급합니다.
                    """
    )
    ResponseEntity<ApiResponse<OAuthLoginResponse.OAuthLogin>> googleLogin(
            OAuthLoginRequest.GoogleIdToken request
    );
}

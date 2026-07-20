package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.dto.request.OAuthLoginRequest;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthLoginResponse;
import com.example.TODAIT__BE.domain.member.service.OAuthService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.apiPayload.code.GeneralSuccessCode;
import com.example.TODAIT__BE.infra.oauth.GoogleOAuthClient;
import com.example.TODAIT__BE.infra.oauth.KakaoOAuthClient;
import com.example.TODAIT__BE.infra.oauth.dto.GoogleUserInfo;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@Tag(
        name = "OAuth",
        description = "카카오/구글 소셜 로그인 API"
)
@SecurityRequirements
public class OAuthController {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final GoogleOAuthClient googleOAuthClient;
    private final OAuthService oAuthService;

    @PostMapping("/api/auth/kakao/login")
    @Operation(
            summary = "카카오 로그인",
            description = """
                    Android에서 발급한 카카오 Access Token을 검증하여 로그인합니다.
                    기존 회원이면 서비스 Access Token과 Refresh Token을 발급합니다.
                    신규 회원이면 소셜 회원가입 완료에 사용할 온보딩 토큰을 발급합니다.
                    """
    )
    public ResponseEntity<ApiResponse<OAuthLoginResponse.OAuthLogin>> kakaoLogin(
            @Valid @RequestBody OAuthLoginRequest.KakaoAccessToken request
    ) {
        OAuthLoginResponse.OAuthLogin response = oAuthService.loginWithKakao(request.accessToken());

        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }


    @PostMapping("/api/auth/google/login")
    @Operation(
            summary = "구글 로그인",
            description = """
                    Android에서 발급한 Google ID Token을 검증하여 로그인합니다.
                    기존 회원이면 서비스 Access Token과 Refresh Token을 발급합니다.
                    신규 회원이면 소셜 회원가입 완료에 사용할 온보딩 토큰을 발급합니다.
                    """
    )
    public ResponseEntity<ApiResponse<OAuthLoginResponse.OAuthLogin>> googleLogin(
            @Valid @RequestBody OAuthLoginRequest.GoogleIdToken request
    ){
        OAuthLoginResponse.OAuthLogin response = oAuthService.loginWithGoogle(request.idToken());

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                    GeneralSuccessCode.OK,
                    response
                )

        );
    }
}

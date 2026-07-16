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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final GoogleOAuthClient googleOAuthClient;
    private final OAuthService oAuthService;

    @PostMapping("/api/auth/kakao/login")
    public ResponseEntity<ApiResponse<OAuthLoginResponse.OAuthLogin>> kakaoLogin(
            @Valid @RequestBody OAuthLoginRequest.Code request
    ) {
        KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.getUserInfo(request.code());
        OAuthLoginResponse.OAuthLogin response = oAuthService.loginWithKakao(kakaoUserInfo);

        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }


    @PostMapping("/api/auth/google/login")
    public ResponseEntity<ApiResponse<OAuthLoginResponse.OAuthLogin>> googleLogin(
            @Valid @RequestBody OAuthLoginRequest.GoogleIdToken request
    ){
        GoogleUserInfo googleUserInfo = googleOAuthClient.verifyIdToken(request.idToken());
        OAuthLoginResponse.OAuthLogin response = oAuthService.loginWithGoogle(googleUserInfo);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                    GeneralSuccessCode.OK,
                    response
                )

        );
    }
}

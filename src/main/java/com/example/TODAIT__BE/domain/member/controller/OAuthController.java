package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.dto.response.OAuthLoginResponse;
import com.example.TODAIT__BE.domain.member.service.OAuthService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.apiPayload.code.GeneralSuccessCode;
import com.example.TODAIT__BE.infra.oauth.KakaoOAuthClient;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final OAuthService oAuthService;

    @GetMapping("/api/auth/kakao")
    public ResponseEntity<Void> redirectToKakao() {
        URI redirectUri = URI.create(kakaoOAuthClient.getAuthorizationUrl());

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirectUri)
                .build();
    }

    @GetMapping("/api/auth/kakao/callback")
    public ResponseEntity<ApiResponse<OAuthLoginResponse.OAuthLogin>> kakaoCallback(
            @RequestParam String code
    ){
        KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.getUserInfo(code);
        OAuthLoginResponse.OAuthLogin response = oAuthService.loginWithKakao(kakaoUserInfo);

        return ResponseEntity.ok(ApiResponse.onSuccess(GeneralSuccessCode.OK, response));
    }
}

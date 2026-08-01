package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.OAuthSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.OAuthControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.OAuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthResponse;
import com.example.TODAIT__BE.domain.member.service.OAuthService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class OAuthController implements OAuthControllerDocs {

    private final OAuthService oauthService;

    @PostMapping("/api/auth/kakao/login")
    @Override
    public ResponseEntity<ApiResponse<OAuthResponse.Login>> kakaoLogin(
            @Valid @RequestBody OAuthRequest.KakaoAccessToken request
    ) {
        OAuthResponse.Login response = oauthService.loginWithKakao(request.accessToken());

        return ResponseEntity
                .status(OAuthSuccessCode.OAUTH_LOGIN_OK.getStatus())
                .body(ApiResponse.onSuccess(OAuthSuccessCode.OAUTH_LOGIN_OK, response));
    }


    @PostMapping("/api/auth/google/login")
    @Override
    public ResponseEntity<ApiResponse<OAuthResponse.Login>> googleLogin(
            @Valid @RequestBody OAuthRequest.GoogleIdToken request
    ) {
        OAuthResponse.Login response = oauthService.loginWithGoogle(request.idToken());

        return ResponseEntity
                .status(OAuthSuccessCode.OAUTH_LOGIN_OK.getStatus())
                .body(ApiResponse.onSuccess(OAuthSuccessCode.OAUTH_LOGIN_OK, response));
    }
}

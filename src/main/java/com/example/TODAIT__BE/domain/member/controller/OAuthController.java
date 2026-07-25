package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.OAuthSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.OAuthControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.OAuthLoginRequest;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthLoginResponse;
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

    private final OAuthService oAuthService;

    @PostMapping("/api/auth/kakao/login")
    @Override
    public ResponseEntity<ApiResponse<OAuthLoginResponse.OAuthLogin>> kakaoLogin(
            @Valid @RequestBody OAuthLoginRequest.KakaoAccessToken request
    ) {
        OAuthLoginResponse.OAuthLogin response = oAuthService.loginWithKakao(request.accessToken());

        return ResponseEntity
                .status(OAuthSuccessCode.OAUTH_LOGIN_OK.getStatus())
                .body(ApiResponse.onSuccess(OAuthSuccessCode.OAUTH_LOGIN_OK, response));
    }


    @PostMapping("/api/auth/google/login")
    @Override
    public ResponseEntity<ApiResponse<OAuthLoginResponse.OAuthLogin>> googleLogin(
            @Valid @RequestBody OAuthLoginRequest.GoogleIdToken request
    ){
        OAuthLoginResponse.OAuthLogin response = oAuthService.loginWithGoogle(request.idToken());

        return ResponseEntity
                .status(OAuthSuccessCode.OAUTH_LOGIN_OK.getStatus())
                .body(ApiResponse.onSuccess(OAuthSuccessCode.OAUTH_LOGIN_OK, response));
    }
}

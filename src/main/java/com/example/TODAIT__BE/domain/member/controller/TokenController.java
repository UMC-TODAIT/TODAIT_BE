package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.AuthSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.TokenControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.TokenRefreshRequest;
import com.example.TODAIT__BE.domain.member.dto.response.TokenRefreshResponse;
import com.example.TODAIT__BE.domain.member.service.TokenRefreshService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/token")
public class TokenController implements TokenControllerDocs {

    private final TokenRefreshService tokenRefreshService;

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse.AccessToken>> refresh(
            @Valid @RequestBody TokenRefreshRequest.Refresh request
            ){
        TokenRefreshResponse.AccessToken response = tokenRefreshService.refresh(request);

        return ResponseEntity.ok(ApiResponse.onSuccess(
                AuthSuccessCode.TOKEN_REFRESHED,
                response
            )
        );
    }
}

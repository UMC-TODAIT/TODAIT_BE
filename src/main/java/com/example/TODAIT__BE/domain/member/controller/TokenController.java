package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.AuthSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.TokenControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.service.AuthService;
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

    private final AuthService authService;

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse.AccessToken>> refresh(
            @Valid @RequestBody AuthRequest.TokenRefresh request
            ){
        AuthResponse.AccessToken response = authService.refresh(request);

        return ResponseEntity
                .status(AuthSuccessCode.TOKEN_REFRESHED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.TOKEN_REFRESHED, response));
    }
}

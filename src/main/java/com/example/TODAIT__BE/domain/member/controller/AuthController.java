package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.AuthSuccessCode;
import com.example.TODAIT__BE.domain.member.code.MemberSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.AuthControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.service.EmailLoginService;
import com.example.TODAIT__BE.domain.member.service.LogoutService;
import com.example.TODAIT__BE.domain.member.service.SignupService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {
    private final SignupService signupService;
    private final EmailLoginService emailLoginService;
    private final LogoutService logoutService;

    @PostMapping("/api/auth/signup")
    @Override
    public ResponseEntity<ApiResponse<AuthResponse.Token>> signup(
            @Valid @RequestBody AuthRequest.SignUp request
            ){
                AuthResponse.Token response = signupService.signup(request);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(
                                ApiResponse.onSuccess(MemberSuccessCode.SIGNUP_COMPLETED, response)
                        );
    }

    @PostMapping("/api/auth/login")
    @Override
    public ResponseEntity<ApiResponse<AuthResponse.Token>> login(
            @Valid @RequestBody AuthRequest.Login request
    ){
        AuthResponse.Token response = emailLoginService.login(request);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        MemberSuccessCode.LOGIN_COMPLETED,
                        response
                )
        );
    }

    @PostMapping("/api/auth/logout")
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody AuthRequest.Logout request
    ) {
        logoutService.logout(request);

        return ResponseEntity
                .status(AuthSuccessCode.LOGOUT_COMPLETED.getStatus())
                .body(ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_COMPLETED, null));
    }
}

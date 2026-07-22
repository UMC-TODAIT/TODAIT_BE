package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.MemberSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.AuthControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.SignRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
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

    @PostMapping("/api/auth/signup")
    @Override
    public ResponseEntity<ApiResponse<AuthTokenResponse.Token>> signup(
            @Valid @RequestBody SignRequest.SignUp request
            ){
                AuthTokenResponse.Token response = signupService.signup(request);

                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(
                                ApiResponse.onSuccess(MemberSuccessCode.SIGNUP_COMPLETED, response)
                        );
    }

}

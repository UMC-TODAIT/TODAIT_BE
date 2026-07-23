package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.EmailVerificationControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationSendRequest;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationVerifyRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationSendResponse;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationVerifyResponse;
import com.example.TODAIT__BE.domain.member.service.EmailVerificationService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email")
public class EmailVerificationController implements EmailVerificationControllerDocs {

    private final EmailVerificationService emailVerificationService;

    public EmailVerificationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/send-code")
    @Override
    public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendVerificationCode(
            @Valid @RequestBody EmailVerificationSendRequest request
    ) {
        EmailVerificationSendResponse response = emailVerificationService.sendVerificationCode(request);
        return ApiResponse.onSuccessResponse(EmailVerificationSuccessCode.CODE_SENT, response);
    }

    @PostMapping("/verify-code")
    @Override
    public ResponseEntity<ApiResponse<EmailVerificationVerifyResponse>> verifyCode(
            @Valid @RequestBody EmailVerificationVerifyRequest request
    ) {
        EmailVerificationVerifyResponse response = emailVerificationService.verifyCode(request);
        return ApiResponse.onSuccessResponse(EmailVerificationSuccessCode.COMPLETED, response);
    }
}

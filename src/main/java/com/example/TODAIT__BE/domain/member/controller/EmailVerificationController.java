package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.EmailVerificationControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationResponse;
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
    public ResponseEntity<ApiResponse<EmailVerificationResponse.Send>> sendVerificationCode(
            @Valid @RequestBody EmailVerificationRequest.Send request
    ) {
        EmailVerificationResponse.Send response = emailVerificationService.sendVerificationCode(request);
        return ResponseEntity
                .status(EmailVerificationSuccessCode.CODE_SENT.getStatus())
                .body(ApiResponse.onSuccess(EmailVerificationSuccessCode.CODE_SENT, response));
    }

    @PostMapping("/verify-code")
    @Override
    public ResponseEntity<ApiResponse<EmailVerificationResponse.Verify>> verifyCode(
            @Valid @RequestBody EmailVerificationRequest.Verify request
    ) {
        EmailVerificationResponse.Verify response = emailVerificationService.verifyCode(request);
        return ResponseEntity
                .status(EmailVerificationSuccessCode.COMPLETED.getStatus())
                .body(ApiResponse.onSuccess(EmailVerificationSuccessCode.COMPLETED, response));
    }
}

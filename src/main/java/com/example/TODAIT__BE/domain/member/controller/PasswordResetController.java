package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.PasswordResetSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.PasswordResetControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.PasswordResetRequest;
import com.example.TODAIT__BE.domain.member.dto.response.PasswordResetResponse;
import com.example.TODAIT__BE.domain.member.service.PasswordResetService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController implements PasswordResetControllerDocs {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/email/send-code")
    @Override
    public ResponseEntity<ApiResponse<PasswordResetResponse.Send>> sendPasswordResetCode(
            @Valid @RequestBody PasswordResetRequest.Send request
    ) {
        PasswordResetResponse.Send response = passwordResetService.sendPasswordResetCode(request);
        return ResponseEntity
                .status(PasswordResetSuccessCode.CODE_SENT.getStatus())
                .body(ApiResponse.onSuccess(PasswordResetSuccessCode.CODE_SENT, response));
    }
}

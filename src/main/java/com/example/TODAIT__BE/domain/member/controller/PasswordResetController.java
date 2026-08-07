package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.PasswordResetSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.PasswordResetControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.PasswordResetRequest;
import com.example.TODAIT__BE.domain.member.dto.response.PasswordResetResponse;
import com.example.TODAIT__BE.domain.member.service.PasswordResetService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
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

    @PostMapping("/email/verify-code")
    @Override
    public ResponseEntity<ApiResponse<PasswordResetResponse.Verify>> verifyPasswordResetCode(
            @Valid @RequestBody PasswordResetRequest.Verify request
    ) {
        PasswordResetResponse.Verify response = passwordResetService.verifyPasswordResetCode(request);
        return ResponseEntity
                .status(PasswordResetSuccessCode.CODE_VERIFIED.getStatus())
                .body(ApiResponse.onSuccess(PasswordResetSuccessCode.CODE_VERIFIED, response));
    }

    @PatchMapping
    @Override
    public ResponseEntity<ApiResponse<PasswordResetResponse.SetNewPassword>> setNewPassword(
            @Valid @RequestBody PasswordResetRequest.SetNewPassword request
    ) {
        PasswordResetResponse.SetNewPassword response = passwordResetService.setNewPassword(request);
        return ResponseEntity
                .status(PasswordResetSuccessCode.PASSWORD_UPDATED.getStatus())
                .body(ApiResponse.onSuccess(PasswordResetSuccessCode.PASSWORD_UPDATED, response));
    }
}

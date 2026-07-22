package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationSendRequest;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationVerifyRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationSendResponse;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationVerifyResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Email Verification", description = "이메일 인증 API")
public interface EmailVerificationControllerDocs {

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입에 사용할 이메일 인증 코드를 발송합니다.")
    ApiResponse<EmailVerificationSendResponse> sendVerificationCode(EmailVerificationSendRequest request);

    @Operation(summary = "이메일 인증 코드 검증", description = "이메일로 발송된 인증 코드를 검증합니다.")
    ApiResponse<EmailVerificationVerifyResponse> verifyCode(EmailVerificationVerifyRequest request);
}

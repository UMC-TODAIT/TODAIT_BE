package com.example.TODAIT__BE.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationVerifyRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바르지 않은 이메일 형식입니다.")
        String email,

        @NotBlank(message = "인증번호는 필수입니다.")
        String code
) {
    public EmailVerificationVerifyRequest {
        email = email == null ? null : email.trim();
    }
}

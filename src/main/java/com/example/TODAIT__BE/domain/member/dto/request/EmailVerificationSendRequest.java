package com.example.TODAIT__BE.domain.member.dto.request;

import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationSendRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바르지 않은 이메일 형식입니다.")
        String email
) {
    public EmailVerificationSendRequest {
        email = MemberInputPolicy.normalizeEmail(email);
    }
}

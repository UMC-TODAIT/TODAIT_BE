package com.example.TODAIT__BE.domain.member.dto.request;

import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class EmailVerificationRequest {

    private EmailVerificationRequest() {
    }

    @Schema(name = "EmailVerificationSendRequest")
    public record Send(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email
    ) {
        public Send {
            email = MemberInputPolicy.normalizeEmail(email);
        }
    }

    @Schema(name = "EmailVerificationVerifyRequest")
    public record Verify(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email,

            @NotBlank(message = "인증번호는 필수입니다.")
            String code
    ) {
        public Verify {
            email = MemberInputPolicy.normalizeEmail(email);
        }
    }
}

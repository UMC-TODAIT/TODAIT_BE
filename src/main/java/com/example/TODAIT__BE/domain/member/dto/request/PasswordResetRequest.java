package com.example.TODAIT__BE.domain.member.dto.request;

import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class PasswordResetRequest {

    private PasswordResetRequest() {
    }

    @Schema(name = "PasswordResetEmailSendRequest")
    public record Send(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email
    ) {
        public Send {
            email = MemberInputPolicy.normalizeEmail(email);
        }
    }

    @Schema(name = "PasswordResetEmailVerifyRequest")
    public record Verify(
            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "올바르지 않은 이메일 형식입니다.")
            String email,

            @NotBlank(message = "인증번호는 필수입니다.")
            @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
            String code
    ) {
        public Verify {
            email = MemberInputPolicy.normalizeEmail(email);
            code = code == null ? null : code.trim();
        }
    }
}

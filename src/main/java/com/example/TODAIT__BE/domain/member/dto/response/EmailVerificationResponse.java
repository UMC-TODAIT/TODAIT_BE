package com.example.TODAIT__BE.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public final class EmailVerificationResponse {

    private EmailVerificationResponse() {
    }

    @Schema(name = "EmailVerificationSendResponse")
    public record Send(
            String email,
            long expiresInMinutes
    ) {
    }

    @Schema(name = "EmailVerificationVerifyResponse")
    public record Verify(
            String email,
            boolean verified
    ) {
    }
}

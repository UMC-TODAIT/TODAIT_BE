package com.example.TODAIT__BE.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public final class PasswordResetResponse {

    private PasswordResetResponse() {
    }

    @Schema(name = "PasswordResetEmailSendResponse")
    public record Send() {
    }
}

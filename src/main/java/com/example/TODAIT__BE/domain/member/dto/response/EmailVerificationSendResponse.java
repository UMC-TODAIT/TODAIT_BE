package com.example.TODAIT__BE.domain.member.dto.response;

public record EmailVerificationSendResponse(
        String email,
        long expiresInMinutes
) {
}

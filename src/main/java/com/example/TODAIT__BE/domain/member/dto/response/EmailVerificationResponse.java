package com.example.TODAIT__BE.domain.member.dto.response;

public class EmailVerificationResponse {

    public record Send(
            String email,
            long expiresInMinutes
    ) {
    }

    public record Verify(
            String email,
            boolean verified
    ) {
    }
}

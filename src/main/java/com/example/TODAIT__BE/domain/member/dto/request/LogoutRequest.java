package com.example.TODAIT__BE.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {
    public record Logout(
            @NotBlank
            String refreshToken
    ) {
    }
}

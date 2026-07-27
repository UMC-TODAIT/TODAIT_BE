package com.example.TODAIT__BE.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public class TokenRefreshRequest {
    public record Refresh(
            @NotBlank
            String refreshToken
    ){}
}

package com.example.TODAIT__BE.infra.oauth.dto;

public record GoogleUserInfo(
        String providerUserId,
        String email,
        String profileImageUrl
) {
}

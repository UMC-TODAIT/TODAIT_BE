package com.example.TODAIT__BE.infra.oauth.dto;

public record KakaoUserInfo(
        String providerUserId,
        String email,
        String profileImageUrl
) {
}

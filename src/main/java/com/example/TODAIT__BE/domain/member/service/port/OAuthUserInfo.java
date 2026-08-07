package com.example.TODAIT__BE.domain.member.service.port;

public record OAuthUserInfo(
        String providerUserId,
        String email,
        String profileImageUrl
) {
}

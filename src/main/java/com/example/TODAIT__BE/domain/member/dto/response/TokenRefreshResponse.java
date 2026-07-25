package com.example.TODAIT__BE.domain.member.dto.response;

import lombok.Builder;

public class TokenRefreshResponse {
    @Builder
    public record AccessToken(
            String accessToken
    ){}
}

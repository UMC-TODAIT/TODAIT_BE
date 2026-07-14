package com.example.TODAIT__BE.domain.member.dto.response;

import lombok.Builder;

public class AuthTokenResponse {

    @Builder
    public record Token(
            String accessToken,
            String refreshToken
    ){
    }
}

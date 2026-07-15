package com.example.TODAIT__BE.domain.member.dto.response;

import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import lombok.Builder;


public class OAuthLoginResponse{

    @Builder
    public record OAuthLogin(
            String loginStatus,
            String accessToken,
            String refreshToken,
            String onboardingToken,
            String email,
            OAuthProvider provider
    ){}

}

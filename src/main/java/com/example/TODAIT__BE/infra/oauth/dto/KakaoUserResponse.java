package com.example.TODAIT__BE.infra.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        Long id,
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            String email,
            Profile profile
    ) {
    }

    public record Profile(
            @JsonProperty("profile_image_url")
            String profileImageUrl,

            @JsonProperty("is_default_image")
            Boolean isDefaultImage
    ) {
    }
}

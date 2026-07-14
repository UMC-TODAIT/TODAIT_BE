package com.example.TODAIT__BE.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public class OAuthLoginRequest {

    public record Code(
            @NotBlank String code
    ){
    }
}

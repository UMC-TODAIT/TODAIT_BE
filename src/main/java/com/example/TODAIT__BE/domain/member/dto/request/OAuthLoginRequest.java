package com.example.TODAIT__BE.domain.member.dto.request;

public class OAuthLoginRequest {

    public record Code(
            String code
    ){
    }
}

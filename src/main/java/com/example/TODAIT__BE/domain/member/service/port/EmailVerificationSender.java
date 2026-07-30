package com.example.TODAIT__BE.domain.member.service.port;

public interface EmailVerificationSender {

    void sendVerificationCode(String email, String code);
}

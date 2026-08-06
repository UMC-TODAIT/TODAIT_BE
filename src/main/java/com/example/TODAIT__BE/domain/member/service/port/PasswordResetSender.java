package com.example.TODAIT__BE.domain.member.service.port;

public interface PasswordResetSender {

    void sendPasswordResetCode(String email, String code);
}

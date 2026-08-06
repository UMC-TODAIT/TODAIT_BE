package com.example.TODAIT__BE.domain.member.service.port;

public interface PasswordResetStore {

    boolean saveCodeIfNotCoolingDown(String email, String code);
}

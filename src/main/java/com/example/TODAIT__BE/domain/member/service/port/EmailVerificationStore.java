package com.example.TODAIT__BE.domain.member.service.port;

public interface EmailVerificationStore {

    boolean saveCodeIfNotCoolingDown(String email, String code);

    void deleteCodeAndCooldownIfMatches(String email, String code);

    VerifyCodeResult verifyCodeAndMarkVerified(String email, String code);

    boolean isVerified(String email);

    enum VerifyCodeResult {
        CODE_NOT_FOUND,
        CODE_MISMATCH,
        VERIFIED,
        VERIFY_ATTEMPT_EXCEEDED
    }
}

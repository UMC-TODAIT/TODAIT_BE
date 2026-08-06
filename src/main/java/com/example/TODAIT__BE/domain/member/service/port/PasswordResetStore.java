package com.example.TODAIT__BE.domain.member.service.port;

public interface PasswordResetStore {

    boolean saveCodeIfNotCoolingDown(String email, String code);

    void deleteCodeAndCooldownIfMatches(String email, String code);

    VerifyCodeResult verifyCodeAndSaveResetToken(String email, String code, String resetToken);

    ConsumeResetTokenResult consumeResetToken(String resetToken);

    enum VerifyCodeResult {
        CODE_NOT_FOUND,
        CODE_MISMATCH,
        CODE_EXPIRED,
        VERIFIED,
        VERIFY_ATTEMPT_EXCEEDED
    }

    record ConsumeResetTokenResult(
            ConsumeResetTokenStatus status,
            String email
    ) {
    }

    enum ConsumeResetTokenStatus {
        VALID,
        INVALID,
        EXPIRED
    }
}

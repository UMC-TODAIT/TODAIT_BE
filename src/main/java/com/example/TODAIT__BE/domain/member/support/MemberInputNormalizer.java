package com.example.TODAIT__BE.domain.member.support;

import java.util.Locale;

public final class MemberInputNormalizer {

    private MemberInputNormalizer() {
    }

    public static String normalizeEmail(String email) {
        return email == null
                ? null
                : email.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeNickname(String nickname) {
        return nickname == null
                ? null
                : nickname.trim();
    }
}

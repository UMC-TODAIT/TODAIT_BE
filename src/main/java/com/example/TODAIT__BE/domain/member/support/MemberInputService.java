package com.example.TODAIT__BE.domain.member.support;

import java.util.Locale;
import java.util.regex.Pattern;

public final class MemberInputService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private MemberInputService() {
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

    public static boolean isValidEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return normalizedEmail != null && EMAIL_PATTERN.matcher(normalizedEmail).matches();
    }
}

package com.example.TODAIT__BE.domain.member.support;

import java.util.Locale;
import java.util.regex.Pattern;

public final class MemberInputPolicy {

    public static final int PROFILE_IMAGE_URL_MAX_LENGTH = 2048;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private MemberInputPolicy() {
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

    public static String normalizeProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl == null) {
            return null;
        }

        String normalizedProfileImageUrl = profileImageUrl.trim();

        if (normalizedProfileImageUrl.isEmpty()
                || normalizedProfileImageUrl.length() > PROFILE_IMAGE_URL_MAX_LENGTH) {
            return null;
        }

        return normalizedProfileImageUrl;
    }

    public static boolean isValidEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return normalizedEmail != null && EMAIL_PATTERN.matcher(normalizedEmail).matches();
    }
}

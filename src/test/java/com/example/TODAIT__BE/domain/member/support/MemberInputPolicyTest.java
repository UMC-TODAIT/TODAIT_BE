package com.example.TODAIT__BE.domain.member.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberInputPolicyTest {

    @Test
    void normalizeProfileImageUrlTrimsValidUrl() {
        String normalized = MemberInputPolicy.normalizeProfileImageUrl(
                "  https://example.com/profile.jpg  "
        );

        assertThat(normalized).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    void normalizeProfileImageUrlReturnsNullForBlankValue() {
        assertThat(MemberInputPolicy.normalizeProfileImageUrl("   ")).isNull();
    }

    @Test
    void normalizeProfileImageUrlReturnsNullWhenMaxLengthIsExceeded() {
        String oversizedUrl = "a".repeat(
                MemberInputPolicy.PROFILE_IMAGE_URL_MAX_LENGTH + 1
        );

        assertThat(MemberInputPolicy.normalizeProfileImageUrl(oversizedUrl)).isNull();
    }
}

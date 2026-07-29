package com.example.TODAIT__BE.domain.member.dto.response;

public final class MemberResponse {

    private MemberResponse() {
    }

    public record Me(
            Long memberId,
            String email,
            String nickname,
            String profileImageUrl,
            long savedCourseCount
    ) {
    }

    public record NicknameAvailability(
            String nickname,
            boolean available
    ) {
    }
}

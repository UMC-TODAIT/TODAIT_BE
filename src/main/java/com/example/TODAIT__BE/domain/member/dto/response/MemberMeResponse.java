package com.example.TODAIT__BE.domain.member.dto.response;

public record MemberMeResponse(
        Long memberId,
        String email,
        String nickname,
        String profileImageUrl,
        long savedCourseCount
) {
}

package com.example.TODAIT__BE.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public final class MemberResponse {

    private MemberResponse() {
    }

    @Schema(name = "MemberMeResponse")
    public record Me(
            Long memberId,
            String email,
            String nickname,
            String profileImageUrl,
            long savedCourseCount
    ) {
    }

    @Schema(name = "NicknameAvailabilityResponse")
    public record NicknameAvailability(
            String nickname,
            boolean available
    ) {
    }
}

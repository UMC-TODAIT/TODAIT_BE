package com.example.TODAIT__BE.domain.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SavedCourseMemoUpdateRequest(

        @Schema(
                description = "수정할 저장 코스 메모. null, 빈 문자열, 공백 문자열은 메모 삭제로 처리",
                example = "힐링하고 싶은 날 즐기는 데이트 코스",
                nullable = true
        )
        String memo
) {
}

package com.example.TODAIT__BE.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SavedCourseMemoUpdateResponse(

        @Schema(
                description = "저장 코스 ID",
                example = "10"
        )
        Long courseId,

        @Schema(
                description = "수정된 저장 코스 메모",
                example = "힐링하고 싶은 날 즐기는 데이트 코스",
                nullable = true
        )
        String memo
) {

    public static SavedCourseMemoUpdateResponse of(
            Long courseId,
            String memo
    ) {
        return new SavedCourseMemoUpdateResponse(
                courseId,
                memo
        );
    }
}

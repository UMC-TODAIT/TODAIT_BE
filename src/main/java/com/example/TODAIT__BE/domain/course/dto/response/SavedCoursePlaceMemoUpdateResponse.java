package com.example.TODAIT__BE.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SavedCoursePlaceMemoUpdateResponse(

        @Schema(
                description = "저장 코스 ID",
                example = "10"
        )
        Long courseId,

        @Schema(
                description = "저장 코스 장소 항목 ID",
                example = "25"
        )
        Long coursePlaceId,

        @Schema(
                description = "수정된 저장 코스 장소 메모",
                example = "조용하고 풍경이 예쁨",
                nullable = true
        )
        String memo
) {

    public static SavedCoursePlaceMemoUpdateResponse of(
            Long courseId,
            Long coursePlaceId,
            String memo
    ) {
        return new SavedCoursePlaceMemoUpdateResponse(
                courseId,
                coursePlaceId,
                memo
        );
    }
}

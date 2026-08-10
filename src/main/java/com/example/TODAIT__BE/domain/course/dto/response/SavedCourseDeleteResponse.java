package com.example.TODAIT__BE.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장 코스 삭제 응답")
public record SavedCourseDeleteResponse(

        @Schema(
                description = "삭제 처리된 저장 코스 ID",
                example = "10"
        )
        Long courseId
) {

    public static SavedCourseDeleteResponse of(Long courseId) {
        return new SavedCourseDeleteResponse(courseId);
    }
}

package com.example.TODAIT__BE.domain.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SavedCoursePlaceMemoUpdateRequest(

        @Schema(
                description = "수정할 저장 코스 장소 메모. null, 빈 문자열, 공백 문자열은 메모 삭제로 처리",
                example = "조용하고 풍경이 예쁨",
                nullable = true
        )
        String memo
) {
}

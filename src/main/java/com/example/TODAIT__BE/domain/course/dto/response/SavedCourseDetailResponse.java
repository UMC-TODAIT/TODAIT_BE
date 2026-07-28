package com.example.TODAIT__BE.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record SavedCourseDetailResponse(
        Long courseId,
        String title,
        LocalDate savedDate,
        RepresentativeMoodTagResponse representativeMoodTag,
        @Schema(description = "저장 코스 상세 상단에 표시할 기준 장소의 세부 카테고리")
        RepresentativeSubCategoryResponse representativePlaceCategory,
        String memo,
        Integer placeCount,
        Integer viewCount,
        List<SavedCourseDetailPlaceResponse> places
) {
}

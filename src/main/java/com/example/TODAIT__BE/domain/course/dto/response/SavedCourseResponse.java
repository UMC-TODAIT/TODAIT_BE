package com.example.TODAIT__BE.domain.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public final class SavedCourseResponse {

    private SavedCourseResponse() {
    }

    public record OverviewResponse(
            List<CardResponse> recentCourses,
            List<CardResponse> popularCourses
    ) {
    }

    public record CardResponse(
            Long courseId,
            String title,
            LocalDate savedDate,
            RepresentativeMoodTag representativeMoodTag,
            RepresentativeSubCategory representativePlaceCategory,
            List<PreviewPlaceResponse> previewPlaces,
            Integer remainingPlaceCount,
            Integer placeCount,
            Integer viewCount
    ) {
    }

    public record PreviewPlaceResponse(
            Long placeId,
            String name,
            Integer visitOrder
    ) {
    }

    public record DetailResponse(
            Long courseId,
            String title,
            LocalDate savedDate,
            RepresentativeMoodTag representativeMoodTag,
            @Schema(description = "저장 코스 상세 상단에 표시할 기준 장소의 세부 카테고리")
            RepresentativeSubCategory representativePlaceCategory,
            String memo,
            Integer placeCount,
            Integer viewCount,
            List<DetailPlaceResponse> places
    ) {
    }

    public record DetailPlaceResponse(
            Long coursePlaceId,
            Long placeId,
            Integer visitOrder,
            String name,
            String address,
            String memo
    ) {
    }

    public record RepresentativeMoodTag(
            Long moodTagId,
            String code,
            String name
    ) {
    }

    public record RepresentativeSubCategory(
            String code,
            String name
    ) {
    }
}

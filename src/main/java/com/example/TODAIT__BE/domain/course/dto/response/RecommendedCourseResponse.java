package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import java.time.LocalDateTime;
import java.util.List;

public final class RecommendedCourseResponse {

    private RecommendedCourseResponse() {
    }

    public record DetailResponse(
            Long courseId,
            String title,
            RepresentativeMoodTag representativeMoodTag,
            RepresentativeSubCategory representativePlaceCategory,
            Integer placeCount,
            List<PlaceResponse> places
    ) {
    }

    public record PlaceResponse(
            Long coursePlaceId,
            Long placeId,
            Integer visitOrder,
            String name,
            String representativeImageUrl,
            String address,
            Double latitude,
            Double longitude
    ) {
    }

    public record SaveResponse(
            Long sourceCourseId,
            Long savedCourseId,
            String title,
            CourseVisibility visibility,
            CourseSourceType sourceType,
            Integer placeCount,
            LocalDateTime savedAt
    ) {

        public static SaveResponse of(
                Long sourceCourseId,
                Course savedCourse,
                int placeCount
        ) {
            return new SaveResponse(
                    sourceCourseId,
                    savedCourse.getId(),
                    savedCourse.getTitle(),
                    savedCourse.getVisibility(),
                    savedCourse.getSourceType(),
                    placeCount,
                    savedCourse.getCreatedAt()
            );
        }
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

package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import java.time.LocalDateTime;
import java.util.List;

public record CourseSaveResponse(
        Long courseId,
        CourseDraftStatus draftStatus,
        String title,
        String memo,
        LocalDateTime savedAt,
        Integer placeCount,
        List<CourseMoodTagResponse> moodTags,
        List<CourseFoodCategoryResponse> foodCategories,
        List<CoursePlaceResponse> places
) {

    public static CourseSaveResponse of(
            Course course,
            List<CourseMoodTagResponse> moodTags,
            List<CourseFoodCategoryResponse> foodCategories,
            List<CoursePlaceResponse> places
    ) {
        return new CourseSaveResponse(
                course.getId(),
                CourseDraftStatus.COMPLETED,
                course.getTitle(),
                course.getMemo(),
                course.getCreatedAt(),
                places.size(),
                moodTags,
                foodCategories,
                places
        );
    }
}

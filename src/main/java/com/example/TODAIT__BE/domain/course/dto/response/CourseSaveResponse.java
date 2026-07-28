package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.Course;
import java.util.List;

public record CourseSaveResponse(
        Long courseId,
        String title,
        String memo,
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
                course.getTitle(),
                course.getMemo(),
                places.size(),
                moodTags,
                foodCategories,
                places
        );
    }
}

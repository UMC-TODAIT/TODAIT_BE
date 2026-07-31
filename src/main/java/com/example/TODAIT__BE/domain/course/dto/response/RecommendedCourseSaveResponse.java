package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import java.time.LocalDateTime;

public record RecommendedCourseSaveResponse(
        Long sourceCourseId,
        Long savedCourseId,
        String title,
        CourseVisibility visibility,
        CourseSourceType sourceType,
        Integer placeCount,
        LocalDateTime savedAt
) {

    public static RecommendedCourseSaveResponse of(
            Long sourceCourseId,
            Course savedCourse,
            int placeCount
    ) {
        return new RecommendedCourseSaveResponse(
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

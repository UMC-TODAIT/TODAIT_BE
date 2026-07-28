package com.example.TODAIT__BE.domain.course.dto.response;

import java.util.List;

public record SavedCourseOverviewResponse(
        List<SavedCourseCardResponse> recentCourses,
        List<SavedCourseCardResponse> popularCourses
) {
}

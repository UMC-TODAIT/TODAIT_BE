package com.example.TODAIT__BE.domain.course.dto.request;

import java.util.List;

public record CourseDraftFoodCategorySaveRequest(
        List<Long> foodCategoryIds
) {
}

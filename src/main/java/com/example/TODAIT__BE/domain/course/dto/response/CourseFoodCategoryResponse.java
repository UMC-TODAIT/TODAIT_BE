package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;

public record CourseFoodCategoryResponse(
        Long foodCategoryId,
        String code,
        String name
) {

    public static CourseFoodCategoryResponse from(FoodCategory foodCategory) {
        return new CourseFoodCategoryResponse(foodCategory.getId(), foodCategory.getCode(), foodCategory.getName());
    }
}

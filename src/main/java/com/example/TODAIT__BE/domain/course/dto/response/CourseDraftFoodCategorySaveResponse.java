package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import java.util.List;

public record CourseDraftFoodCategorySaveResponse(
        Long courseDraftId,
        CourseDraftStatus draftStatus,
        List<FoodCategoryItem> foodCategories
) {

    public static CourseDraftFoodCategorySaveResponse of(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            List<FoodCategory> foodCategories
    ) {
        return new CourseDraftFoodCategorySaveResponse(
                courseDraftId,
                draftStatus,
                foodCategories.stream().map(FoodCategoryItem::from).toList()
        );
    }

    public record FoodCategoryItem(
            Long foodCategoryId,
            String code,
            String name
    ) {
        public static FoodCategoryItem from(FoodCategory foodCategory) {
            return new FoodCategoryItem(foodCategory.getId(), foodCategory.getCode(), foodCategory.getName());
        }
    }
}

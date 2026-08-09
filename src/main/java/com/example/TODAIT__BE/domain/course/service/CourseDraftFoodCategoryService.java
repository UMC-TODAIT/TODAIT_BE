package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftFoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftFoodCategorySaveResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.taxonomy.code.FoodCategoryErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.FoodCategoryRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseDraftFoodCategoryService {

    private static final int MIN_FOOD_CATEGORY_COUNT = 1;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final CourseDraftValidator courseDraftValidator;

    @Transactional
    public CourseDraftFoodCategorySaveResponse saveFoodCategories(
            Long courseDraftId,
            Long memberId,
            CourseDraftFoodCategorySaveRequest request
    ) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND));

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatusIn(
                courseDraft,
                CourseDraftErrorCode.FOOD_CATEGORY_DRAFT_STATUS_CONFLICT,
                CourseDraftStatus.FOOD_SELECTING,
                CourseDraftStatus.BASE_PLACE_SELECTING
        );

        List<Long> foodCategoryIds = request.foodCategoryIds();

        if (foodCategoryIds == null || foodCategoryIds.size() < MIN_FOOD_CATEGORY_COUNT) {
            throw new CourseException(CourseDraftErrorCode.INVALID_FOOD_CATEGORY_COUNT);
        }

        if (new HashSet<>(foodCategoryIds).size() != foodCategoryIds.size()) {
            throw new CourseException(CourseDraftErrorCode.DUPLICATE_FOOD_CATEGORY);
        }

        List<FoodCategory> foodCategories = validateAndGetFoodCategories(foodCategoryIds);

        updateFoodCategories(courseDraft, foodCategories);

        courseDraft.changeStatus(CourseDraftStatus.BASE_PLACE_SELECTING);

        return CourseDraftFoodCategorySaveResponse.of(
                courseDraft.getId(),
                courseDraft.getStatus(),
                foodCategories
        );
    }

    private List<FoodCategory> validateAndGetFoodCategories(List<Long> foodCategoryIds) {
        List<FoodCategory> foundFoodCategories = foodCategoryRepository.findAllById(foodCategoryIds);
        if (foundFoodCategories.size() != foodCategoryIds.size()) {
            throw new TaxonomyException(FoodCategoryErrorCode.FOOD_CATEGORY_NOT_FOUND);
        }

        Map<Long, FoodCategory> foodCategoriesById = foundFoodCategories.stream()
                .collect(Collectors.toMap(FoodCategory::getId, Function.identity()));
        return foodCategoryIds.stream()
                .map(foodCategoriesById::get)
                .toList();
    }

    private void updateFoodCategories(CourseDraft courseDraft, List<FoodCategory> foodCategories) {
        List<CourseDraftFoodCategory> existingFoodCategories =
                courseDraftFoodCategoryRepository.findByCourseDraft(courseDraft);
        Set<Long> requestedFoodCategoryIds = foodCategories.stream()
                .map(FoodCategory::getId)
                .collect(Collectors.toSet());
        Set<Long> existingFoodCategoryIds = existingFoodCategories.stream()
                .map(courseDraftFoodCategory -> courseDraftFoodCategory.getFoodCategory().getId())
                .collect(Collectors.toSet());

        List<CourseDraftFoodCategory> foodCategoriesToDelete = existingFoodCategories.stream()
                .filter(courseDraftFoodCategory ->
                        !requestedFoodCategoryIds.contains(courseDraftFoodCategory.getFoodCategory().getId()))
                .toList();
        courseDraftFoodCategoryRepository.deleteAll(foodCategoriesToDelete);

        foodCategories.stream()
                .filter(foodCategory -> !existingFoodCategoryIds.contains(foodCategory.getId()))
                .map(foodCategory -> CourseDraftFoodCategory.builder()
                        .courseDraft(courseDraft)
                        .foodCategory(foodCategory)
                        .build())
                .forEach(courseDraftFoodCategoryRepository::save);
    }
}

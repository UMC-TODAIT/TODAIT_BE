package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.FoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.FoodCategorySaveResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.taxonomy.code.FoodCategoryErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.FoodCategoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseDraftFoodCategoryServiceTest {

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    @Mock
    private FoodCategoryRepository foodCategoryRepository;

    private CourseDraftService courseDraftService;

    @BeforeEach
    void setUp() {
        courseDraftService = new CourseDraftService(
                courseDraftRepository,
                null,
                null,
                courseDraftFoodCategoryRepository,
                null,
                null,
                foodCategoryRepository,
                null,
                null,
                null,
                null,
                null,
                null,
                new CourseDraftValidator()
        );
    }

    @Test
    void updatesFoodCategoriesByDiffAndMovesDraftToBasePlaceSelecting() {
        CourseDraft draft = draft(CourseDraftStatus.FOOD_SELECTING);
        FoodCategory oldFoodCategory = foodCategory(1L, "KOREAN", "한식");
        FoodCategory keptFoodCategory = foodCategory(2L, "JAPANESE", "일식");
        FoodCategory addedFoodCategory = foodCategory(3L, "WESTERN", "양식");
        CourseDraftFoodCategory oldDraftFoodCategory = CourseDraftFoodCategory.builder()
                .courseDraft(draft)
                .foodCategory(oldFoodCategory)
                .build();
        CourseDraftFoodCategory keptDraftFoodCategory = CourseDraftFoodCategory.builder()
                .courseDraft(draft)
                .foodCategory(keptFoodCategory)
                .build();

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(foodCategoryRepository.findByIdInAndIsActiveTrue(List.of(2L, 3L)))
                .willReturn(List.of(keptFoodCategory, addedFoodCategory));
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft))
                .willReturn(List.of(oldDraftFoodCategory, keptDraftFoodCategory));

        FoodCategorySaveResponse response = courseDraftService.saveFoodCategories(
                10L,
                1L,
                new FoodCategorySaveRequest(List.of(2L, 3L))
        );

        verify(courseDraftRepository).findByIdForUpdate(10L);
        verify(courseDraftFoodCategoryRepository).deleteAll(List.of(oldDraftFoodCategory));

        ArgumentCaptor<CourseDraftFoodCategory> saveCaptor = ArgumentCaptor.forClass(CourseDraftFoodCategory.class);
        verify(courseDraftFoodCategoryRepository).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue().getFoodCategory()).isEqualTo(addedFoodCategory);
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.BASE_PLACE_SELECTING);
        assertThat(response.foodCategories()).extracting("foodCategoryId").containsExactly(2L, 3L);
    }

    @Test
    void keepsBasePlaceSelectingStatusWhenFoodCategoriesAreUpdatedFromBasePlaceScreen() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        FoodCategory foodCategory = foodCategory(1L, "KOREAN", "한식");
        FoodCategory secondFoodCategory = foodCategory(2L, "JAPANESE", "일식");
        CourseDraftFoodCategory existingFoodCategory = CourseDraftFoodCategory.builder()
                .courseDraft(draft)
                .foodCategory(foodCategory)
                .build();
        CourseDraftFoodCategory secondExistingFoodCategory = CourseDraftFoodCategory.builder()
                .courseDraft(draft)
                .foodCategory(secondFoodCategory)
                .build();

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(foodCategoryRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L)))
                .willReturn(List.of(foodCategory, secondFoodCategory));
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft))
                .willReturn(List.of(existingFoodCategory, secondExistingFoodCategory));

        FoodCategorySaveResponse response = courseDraftService.saveFoodCategories(
                10L,
                1L,
                new FoodCategorySaveRequest(List.of(1L, 2L))
        );

        verify(courseDraftFoodCategoryRepository).deleteAll(List.of());
        verify(courseDraftFoodCategoryRepository, never()).save(any());
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.BASE_PLACE_SELECTING);
    }

    @Test
    void throwsWhenFoodCategoriesAreUpdatedInUnsupportedStatus() {
        CourseDraft draft = draft(CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftService.saveFoodCategories(
                10L,
                1L,
                new FoodCategorySaveRequest(List.of(1L))
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.FOOD_CATEGORY_DRAFT_STATUS_CONFLICT);

        verify(courseDraftFoodCategoryRepository, never()).findByCourseDraft(any());
    }

    @Test
    void throwsWhenFoodCategoryIsInactive() {
        CourseDraft draft = draft(CourseDraftStatus.FOOD_SELECTING);

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(foodCategoryRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L)))
                .willReturn(List.of(mock(FoodCategory.class)));

        assertThatThrownBy(() -> courseDraftService.saveFoodCategories(
                10L,
                1L,
                new FoodCategorySaveRequest(List.of(1L, 2L))
        ))
                .isInstanceOf(TaxonomyException.class)
                .extracting("errorCode")
                .isEqualTo(FoodCategoryErrorCode.FOOD_CATEGORY_NOT_FOUND);

        verify(courseDraftFoodCategoryRepository, never()).findByCourseDraft(any());
    }

    private CourseDraft draft(CourseDraftStatus status) {
        return CourseDraft.builder()
                .id(10L)
                .member(Member.builder().id(1L).build())
                .status(status)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    private FoodCategory foodCategory(Long id, String code, String name) {
        FoodCategory foodCategory = mock(FoodCategory.class);
        given(foodCategory.getId()).willReturn(id);
        lenient().when(foodCategory.getCode()).thenReturn(code);
        lenient().when(foodCategory.getName()).thenReturn(name);
        return foodCategory;
    }
}

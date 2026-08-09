package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest.SaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse.FoodCategoryItem;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse.MoodTagItem;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse.CoursePlaceItem;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse.SaveResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.code.CourseSaveErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.course.service.support.CourseSaveSupport;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import java.util.Collections;
import java.util.Comparator;
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
public class CourseSaveService {

    private static final int MIN_MOOD_TAG_COUNT = 2;
    private static final int MAX_MOOD_TAG_COUNT = 6;
    private static final int SELECTED_PLACE_START_ORDER = 2;
    private static final int MAX_TITLE_LENGTH = 255;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;
    private final CourseRepository courseRepository;
    private final MoodTagRepository moodTagRepository;
    private final CourseDraftValidator courseDraftValidator;
    private final CourseSaveSupport courseSaveSupport;

    @Transactional
    public SaveResponse saveCourse(Long courseDraftId, Long memberId, SaveRequest request) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND));

        courseDraftValidator.validateOwner(courseDraft, memberId);

        validateSavableDraft(courseDraft);

        String title = validateAndNormalizeTitle(request.title());
        String memo = normalizeMemo(request.memo());
        List<MoodTag> moodTags = validateAndGetMoodTags(request.moodTagIds());

        List<CourseDraftFoodCategory> draftFoodCategories =
                courseDraftFoodCategoryRepository.findByCourseDraft(courseDraft);
        if (draftFoodCategories.isEmpty()) {
            throw new CourseException(CourseSaveErrorCode.FOOD_CATEGORY_NOT_SELECTED);
        }
        validateDraftFoodCategories(draftFoodCategories);

        ValidatedDraftPlaces validatedDraftPlaces = validateAndGetDraftPlaces(courseDraft);
        List<CourseDraftPlace> draftPlaces = validatedDraftPlaces.draftPlaces();
        CourseDraftPlace baseDraftPlace = validatedDraftPlaces.baseDraftPlace();
        Place basePlace = baseDraftPlace.getPlace();

        Course course = courseRepository.save(Course.builder()
                .member(courseDraft.getMember())
                .basePlace(basePlace)
                .area(basePlace.getArea())
                .title(title)
                .memo(memo)
                .visibility(CourseVisibility.PRIVATE)
                .sourceType(CourseSourceType.USER_CREATED)
                .build());

        List<MoodTagItem> moodTagResponses = courseSaveSupport.saveMoodTags(course, moodTags);
        List<FoodCategoryItem> foodCategoryResponses =
                courseSaveSupport.saveFoodCategories(course, draftFoodCategories);
        List<CoursePlaceItem> placeResponses = courseSaveSupport.savePlaces(course, draftPlaces);

        courseDraft.completeWithCourse(course);
        courseDraftRepository.save(courseDraft);

        return SaveResponse.of(course, moodTagResponses, foodCategoryResponses, placeResponses);
    }

    private String validateAndNormalizeTitle(String rawTitle) {
        String title = rawTitle == null ? null : rawTitle.trim();
        if (title == null || title.isEmpty() || title.length() > MAX_TITLE_LENGTH) {
            throw new CourseException(CourseSaveErrorCode.INVALID_COURSE_TITLE);
        }
        return title;
    }

    private String normalizeMemo(String rawMemo) {
        if (rawMemo == null || rawMemo.isBlank()) {
            return null;
        }
        return rawMemo.trim();
    }

    private List<MoodTag> validateAndGetMoodTags(List<Long> moodTagIds) {
        if (moodTagIds == null
                || moodTagIds.size() < MIN_MOOD_TAG_COUNT
                || moodTagIds.size() > MAX_MOOD_TAG_COUNT
                || new HashSet<>(moodTagIds).size() != moodTagIds.size()) {
            throw new CourseException(CourseDraftErrorCode.INVALID_MOOD_TAG_COUNT);
        }

        List<MoodTag> foundMoodTags = moodTagRepository.findByIdInAndIsActiveTrue(moodTagIds);
        if (foundMoodTags.size() != moodTagIds.size()) {
            throw new CourseException(CourseSaveErrorCode.COURSE_MOOD_TAG_NOT_FOUND);
        }

        Map<Long, MoodTag> moodTagsById = foundMoodTags.stream()
                .collect(Collectors.toMap(MoodTag::getId, Function.identity()));
        return moodTagIds.stream()
                .map(moodTagsById::get)
                .toList();
    }

    private void validateDraftFoodCategories(List<CourseDraftFoodCategory> draftFoodCategories) {
        boolean hasUnavailableFoodCategory = draftFoodCategories.stream()
                .map(CourseDraftFoodCategory::getFoodCategory)
                .anyMatch(foodCategory -> foodCategory == null || !Boolean.TRUE.equals(foodCategory.getIsActive()));
        if (hasUnavailableFoodCategory) {
            throw new CourseException(CourseSaveErrorCode.COURSE_FOOD_CATEGORY_NOT_FOUND);
        }
    }

    private void validateSavableDraft(CourseDraft courseDraft) {
        if (courseDraft.getStatus() == CourseDraftStatus.COMPLETED) {
            Long completedCourseId = courseDraft.getCourse() == null ? null : courseDraft.getCourse().getId();
            throw new CourseException(
                    CourseSaveErrorCode.COURSE_DRAFT_ALREADY_COMPLETED,
                    Collections.singletonMap("courseId", completedCourseId)
            );
        }

        if (courseDraft.getStatus() != CourseDraftStatus.SAVING) {
            throw new CourseException(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
        }
    }

    private ValidatedDraftPlaces validateAndGetDraftPlaces(CourseDraft courseDraft) {
        List<CourseDraftPlace> draftPlaces =
                courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(courseDraft);

        CourseDraftPlace baseDraftPlace = getBaseDraftPlace(draftPlaces);

        Set<Long> placeIds = new HashSet<>();
        for (CourseDraftPlace draftPlace : draftPlaces) {
            if (!placeIds.add(draftPlace.getPlace().getId())) {
                throw new CourseException(CourseDraftErrorCode.INVALID_SELECTED_PLACE);
            }
        }

        List<CourseDraftPlace> selectedPlaces = draftPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.SELECTED)
                .sorted(Comparator.comparing(CourseDraftPlace::getVisitOrder))
                .toList();
        if (selectedPlaces.isEmpty()) {
            throw new CourseException(CourseDraftErrorCode.INVALID_SELECTED_PLACE);
        }

        for (int i = 0; i < selectedPlaces.size(); i++) {
            int expectedOrder = SELECTED_PLACE_START_ORDER + i;
            if (!selectedPlaces.get(i).getVisitOrder().equals(expectedOrder)) {
                throw new CourseException(CourseDraftErrorCode.INVALID_VISIT_ORDER);
            }
        }

        return new ValidatedDraftPlaces(draftPlaces, baseDraftPlace);
    }

    private record ValidatedDraftPlaces(
            List<CourseDraftPlace> draftPlaces,
            CourseDraftPlace baseDraftPlace
    ) {
    }

    private CourseDraftPlace getBaseDraftPlace(List<CourseDraftPlace> draftPlaces) {
        List<CourseDraftPlace> basePlaces = draftPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.BASE)
                .toList();
        if (basePlaces.size() != 1 || !basePlaces.get(0).getVisitOrder().equals(1)) {
            throw new CourseException(CourseDraftErrorCode.INVALID_BASE_PLACE);
        }

        CourseDraftPlace baseDraftPlace = basePlaces.get(0);
        if (baseDraftPlace.getPlace() == null) {
            throw new CourseException(CourseDraftErrorCode.INVALID_BASE_PLACE);
        }

        return baseDraftPlace;
    }

}

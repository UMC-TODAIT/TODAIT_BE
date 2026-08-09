package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseFoodCategoryResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseMoodTagResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CoursePlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import java.util.ArrayList;
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
    private final CourseMoodTagRepository courseMoodTagRepository;
    private final CourseFoodCategoryRepository courseFoodCategoryRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final MoodTagRepository moodTagRepository;
    private final CourseDraftValidator courseDraftValidator;

    @Transactional
    public CourseSaveResponse saveCourse(Long courseDraftId, Long memberId, CourseSaveRequest request) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        courseDraftValidator.validateOwner(courseDraft, memberId);

        validateSavableDraft(courseDraft);

        String title = validateAndNormalizeTitle(request.title());
        String memo = normalizeMemo(request.memo());
        List<MoodTag> moodTags = validateAndGetMoodTags(request.moodTagIds());

        List<CourseDraftFoodCategory> draftFoodCategories =
                courseDraftFoodCategoryRepository.findByCourseDraft(courseDraft);
        if (draftFoodCategories.isEmpty()) {
            throw new CourseException(CourseErrorCode.FOOD_CATEGORY_NOT_SELECTED);
        }

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

        List<CourseMoodTagResponse> moodTagResponses = saveCourseMoodTags(course, moodTags);
        List<CourseFoodCategoryResponse> foodCategoryResponses = saveCourseFoodCategories(course, draftFoodCategories);
        List<CoursePlaceResponse> placeResponses = saveCoursePlaces(course, draftPlaces);

        courseDraft.completeWithCourse(course);
        courseDraftRepository.save(courseDraft);

        return CourseSaveResponse.of(course, moodTagResponses, foodCategoryResponses, placeResponses);
    }

    private String validateAndNormalizeTitle(String rawTitle) {
        String title = rawTitle == null ? null : rawTitle.trim();
        if (title == null || title.isEmpty() || title.length() > MAX_TITLE_LENGTH) {
            throw new CourseException(CourseErrorCode.INVALID_COURSE_TITLE);
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
            throw new CourseException(CourseErrorCode.INVALID_MOOD_TAG_COUNT);
        }

        List<MoodTag> foundMoodTags = moodTagRepository.findByIdInAndIsActiveTrue(moodTagIds);
        if (foundMoodTags.size() != moodTagIds.size()) {
            throw new CourseException(CourseErrorCode.COURSE_MOOD_TAG_NOT_FOUND);
        }

        Map<Long, MoodTag> moodTagsById = foundMoodTags.stream()
                .collect(Collectors.toMap(MoodTag::getId, Function.identity()));
        return moodTagIds.stream()
                .map(moodTagsById::get)
                .toList();
    }

    private void validateSavableDraft(CourseDraft courseDraft) {
        if (courseDraft.getStatus() == CourseDraftStatus.COMPLETED) {
            Long completedCourseId = courseDraft.getCourse() == null ? null : courseDraft.getCourse().getId();
            throw new CourseException(
                    CourseErrorCode.COURSE_DRAFT_ALREADY_COMPLETED,
                    Collections.singletonMap("courseId", completedCourseId)
            );
        }

        if (courseDraft.getStatus() != CourseDraftStatus.SAVING) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
        }
    }

    private ValidatedDraftPlaces validateAndGetDraftPlaces(CourseDraft courseDraft) {
        List<CourseDraftPlace> draftPlaces =
                courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(courseDraft);

        CourseDraftPlace baseDraftPlace = getBaseDraftPlace(draftPlaces);

        Set<Long> placeIds = new HashSet<>();
        for (CourseDraftPlace draftPlace : draftPlaces) {
            if (!placeIds.add(draftPlace.getPlace().getId())) {
                throw new CourseException(CourseErrorCode.INVALID_SELECTED_PLACE);
            }
        }

        List<CourseDraftPlace> selectedPlaces = draftPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.SELECTED)
                .sorted(Comparator.comparing(CourseDraftPlace::getVisitOrder))
                .toList();
        if (selectedPlaces.isEmpty()) {
            throw new CourseException(CourseErrorCode.INVALID_SELECTED_PLACE);
        }

        for (int i = 0; i < selectedPlaces.size(); i++) {
            int expectedOrder = SELECTED_PLACE_START_ORDER + i;
            if (!selectedPlaces.get(i).getVisitOrder().equals(expectedOrder)) {
                throw new CourseException(CourseErrorCode.INVALID_VISIT_ORDER);
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
            throw new CourseException(CourseErrorCode.INVALID_BASE_PLACE);
        }

        CourseDraftPlace baseDraftPlace = basePlaces.get(0);
        if (baseDraftPlace.getPlace() == null) {
            throw new CourseException(CourseErrorCode.INVALID_BASE_PLACE);
        }

        return baseDraftPlace;
    }

    private List<CourseMoodTagResponse> saveCourseMoodTags(Course course, List<MoodTag> moodTags) {
        List<CourseMoodTagResponse> responses = new ArrayList<>();
        for (MoodTag moodTag : moodTags) {
            courseMoodTagRepository.save(CourseMoodTag.builder()
                    .course(course)
                    .moodTag(moodTag)
                    .build());
            responses.add(CourseMoodTagResponse.from(moodTag));
        }
        return responses;
    }

    private List<CourseFoodCategoryResponse> saveCourseFoodCategories(
            Course course,
            List<CourseDraftFoodCategory> draftFoodCategories
    ) {
        List<CourseFoodCategoryResponse> responses = new ArrayList<>();
        for (CourseDraftFoodCategory draftFoodCategory : draftFoodCategories) {
            FoodCategory foodCategory = draftFoodCategory.getFoodCategory();
            courseFoodCategoryRepository.save(CourseFoodCategory.builder()
                    .course(course)
                    .foodCategory(foodCategory)
                    .build());
            responses.add(CourseFoodCategoryResponse.from(foodCategory));
        }
        return responses;
    }

    private List<CoursePlaceResponse> saveCoursePlaces(Course course, List<CourseDraftPlace> draftPlaces) {
        List<CoursePlaceResponse> responses = new ArrayList<>();
        for (CourseDraftPlace draftPlace : draftPlaces) {
            Place place = draftPlace.getPlace();
            CoursePlace coursePlace = coursePlaceRepository.save(CoursePlace.builder()
                    .course(course)
                    .place(place)
                    .visitOrder(draftPlace.getVisitOrder())
                    .placeRole(draftPlace.getPlaceRole())
                    .placeNameSnapshot(place.getName())
                    .addressSnapshot(place.getAddress())
                    .latitudeSnapshot(place.getLatitude())
                    .longitudeSnapshot(place.getLongitude())
                    .categorySnapshot(place.getPlaceCategory().getCode())
                    .memo(draftPlace.getMemo())
                    .build());
            responses.add(CoursePlaceResponse.from(coursePlace));
        }
        return responses;
    }
}

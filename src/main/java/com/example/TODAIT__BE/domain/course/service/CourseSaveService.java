package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseFoodCategoryResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseMoodTagResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CoursePlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomyErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseSaveService {

    private static final int MIN_MOOD_TAG_COUNT = 2;
    private static final int MAX_MOOD_TAG_COUNT = 6;
    private static final int SELECTED_PLACE_START_ORDER = 2;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    private final CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;
    private final CourseRepository courseRepository;
    private final CourseMoodTagRepository courseMoodTagRepository;
    private final CourseFoodCategoryRepository courseFoodCategoryRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    @Transactional
    public CourseSaveResponse saveCourse(Long courseDraftId, Long memberId, CourseSaveRequest request) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED);
        }

        validateSavableDraft(courseDraft);

        String title = request.title();
        if (title == null || title.isBlank()) {
            throw new CourseException(CourseErrorCode.INVALID_COURSE_TITLE);
        }

        List<CourseDraftMoodTag> draftMoodTags = courseDraftMoodTagRepository.findByCourseDraft(courseDraft);
        List<MoodTag> moodTags = validateAndGetMoodTags(draftMoodTags);

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
                .memo(request.memo())
                .visibility(CourseVisibility.PRIVATE)
                .sourceType(CourseSourceType.USER_CREATED)
                .placeCount(draftPlaces.size())
                .build());

        List<CourseMoodTagResponse> moodTagResponses = saveCourseMoodTags(course, moodTags);
        List<CourseFoodCategoryResponse> foodCategoryResponses = saveCourseFoodCategories(course, draftFoodCategories);
        List<CoursePlaceResponse> placeResponses = saveCoursePlaces(course, draftPlaces);

        courseDraft.complete();
        courseDraftRepository.save(courseDraft);

        return CourseSaveResponse.of(course, moodTagResponses, foodCategoryResponses, placeResponses);
    }

    private List<MoodTag> validateAndGetMoodTags(List<CourseDraftMoodTag> draftMoodTags) {
        if (draftMoodTags == null
                || draftMoodTags.size() < MIN_MOOD_TAG_COUNT
                || draftMoodTags.size() > MAX_MOOD_TAG_COUNT) {
            throw new CourseException(CourseErrorCode.INVALID_MOOD_TAG_COUNT);
        }

        List<MoodTag> moodTags = draftMoodTags.stream()
                .map(CourseDraftMoodTag::getMoodTag)
                .toList();
        if (moodTags.stream().anyMatch(moodTag -> moodTag == null)) {
            throw new TaxonomyException(TaxonomyErrorCode.MOOD_TAG_NOT_FOUND);
        }
        return moodTags;
    }

    private void validateSavableDraft(CourseDraft courseDraft) {
        if (courseDraft.getStatus() == CourseDraftStatus.COMPLETED) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_ALREADY_COMPLETED);
        }
        if (courseDraft.getStatus() != CourseDraftStatus.ORDERING
                || courseDraft.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CourseException(CourseErrorCode.INVALID_COURSE_DRAFT_STATUS);
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

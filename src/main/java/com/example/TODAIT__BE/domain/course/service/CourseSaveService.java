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
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import java.util.ArrayList;
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

    private static final int MIN_MOOD_TAG_COUNT = 1;
    private static final int MAX_MOOD_TAG_COUNT = 6;
    private static final int SELECTED_PLACE_START_ORDER = 2;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;
    private final MoodTagRepository moodTagRepository;
    private final CourseRepository courseRepository;
    private final CourseMoodTagRepository courseMoodTagRepository;
    private final CourseFoodCategoryRepository courseFoodCategoryRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    @Transactional
    public CourseSaveResponse saveCourse(Long courseDraftId, Long memberId, CourseSaveRequest request) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(CourseErrorCode.NOT_COURSE_DRAFT_OWNER);
        }

        if (courseDraft.getStatus() == CourseDraftStatus.COMPLETED) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_ALREADY_COMPLETED);
        }

        String title = request.title();
        if (title == null || title.isBlank()) {
            throw new CourseException(CourseErrorCode.INVALID_COURSE_TITLE);
        }

        List<MoodTag> moodTags = validateAndGetMoodTags(request.moodTagIds());

        List<CourseDraftFoodCategory> draftFoodCategories =
                courseDraftFoodCategoryRepository.findByCourseDraft(courseDraft);
        if (draftFoodCategories.isEmpty()) {
            throw new CourseException(CourseErrorCode.FOOD_CATEGORY_NOT_SELECTED);
        }

        List<CourseDraftPlace> draftPlaces = validateAndGetDraftPlaces(courseDraft);
        CourseDraftPlace baseDraftPlace = getBaseDraftPlace(courseDraft, draftPlaces);
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

    private List<MoodTag> validateAndGetMoodTags(List<Long> moodTagIds) {
        if (moodTagIds == null
                || moodTagIds.size() < MIN_MOOD_TAG_COUNT
                || moodTagIds.size() > MAX_MOOD_TAG_COUNT
                || new HashSet<>(moodTagIds).size() != moodTagIds.size()) {
            throw new CourseException(CourseErrorCode.INVALID_MOOD_TAG_COUNT);
        }

        List<MoodTag> foundMoodTags = moodTagRepository.findAllById(moodTagIds);
        if (foundMoodTags.size() != moodTagIds.size()) {
            throw new CourseException(CourseErrorCode.MOOD_TAG_NOT_FOUND);
        }

        Map<Long, MoodTag> moodTagsById = foundMoodTags.stream()
                .collect(Collectors.toMap(MoodTag::getId, Function.identity()));
        return moodTagIds.stream()
                .map(moodTagsById::get)
                .toList();
    }

    private List<CourseDraftPlace> validateAndGetDraftPlaces(CourseDraft courseDraft) {
        List<CourseDraftPlace> draftPlaces =
                courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(courseDraft);

        getBaseDraftPlace(courseDraft, draftPlaces);

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

        return draftPlaces;
    }

    private CourseDraftPlace getBaseDraftPlace(CourseDraft courseDraft, List<CourseDraftPlace> draftPlaces) {
        List<CourseDraftPlace> basePlaces = draftPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.BASE)
                .toList();
        if (basePlaces.size() != 1 || !basePlaces.get(0).getVisitOrder().equals(1)) {
            throw new CourseException(CourseErrorCode.INVALID_BASE_PLACE);
        }

        CourseDraftPlace baseDraftPlace = basePlaces.get(0);
        if (courseDraft.getBasePlace() == null
                || baseDraftPlace.getPlace() == null
                || !baseDraftPlace.getPlace().getId().equals(courseDraft.getBasePlace().getId())) {
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

package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseResponse.RepresentativeMoodTag;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseResponse.RepresentativeSubCategory;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseResponse.CardResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseResponse.DetailPlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseResponse.DetailResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseResponse.OverviewResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseResponse.PreviewPlaceResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.code.SavedCourseErrorCode;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.TODAIT__BE.domain.course.dto.request.SavedCourseMemoUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.SavedCoursePlaceMemoUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseMemoUpdateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCoursePlaceMemoUpdateResponse;

@Service
public class SavedCourseService {

    private static final int COURSE_LIMIT = 2;
    private static final int PREVIEW_PLACE_LIMIT = 3;

    private final CourseRepository courseRepository;
    private final CourseMoodTagRepository courseMoodTagRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    public SavedCourseService(
            CourseRepository courseRepository,
            CourseMoodTagRepository courseMoodTagRepository,
            CoursePlaceRepository coursePlaceRepository
    ) {
        this.courseRepository = courseRepository;
        this.courseMoodTagRepository = courseMoodTagRepository;
        this.coursePlaceRepository = coursePlaceRepository;
    }

    @Transactional(readOnly = true)
    public OverviewResponse getSavedCourseOverview(Long memberId) {
        PageRequest limitTwo = PageRequest.of(0, COURSE_LIMIT);

        List<Course> recentCourses =
                courseRepository
                        .findRecentSavedCourses(
                                memberId,
                                limitTwo
                        );

        List<Course> popularCourses =
                courseRepository
                        .findPopularSavedCourses(
                                memberId,
                                limitTwo
                        );

        Set<Long> courseIdSet = new LinkedHashSet<>();

        recentCourses.stream()
                .map(Course::getId)
                .forEach(courseIdSet::add);

        popularCourses.stream()
                .map(Course::getId)
                .forEach(courseIdSet::add);

        if (courseIdSet.isEmpty()) {
            return new OverviewResponse(
                    List.of(),
                    List.of()
            );
        }

        List<Long> courseIds = new ArrayList<>(courseIdSet);

        Map<Long, RepresentativeMoodTag> moodTagByCourseId =
                getMoodTagByCourseId(courseIds);

        Map<Long, List<CoursePlace>> coursePlacesByCourseId =
                getCoursePlacesByCourseId(courseIds);

        List<CardResponse> recentCourseResponses =
                recentCourses.stream()
                        .map(course ->
                                toCardResponse(
                                        course,
                                        moodTagByCourseId,
                                        coursePlacesByCourseId
                                )
                        )
                        .toList();

        List<CardResponse> popularCourseResponses =
                popularCourses.stream()
                        .map(course ->
                                toCardResponse(
                                        course,
                                        moodTagByCourseId,
                                        coursePlacesByCourseId
                                )
                        )
                        .toList();

        return new OverviewResponse(
                recentCourseResponses,
                popularCourseResponses
        );
    }

    @Transactional
    public DetailResponse getSavedCourseDetail(
            Long memberId,
            Long courseId
    ) {
        Course course = courseRepository
                .findSavedCourseDetailById(courseId)
                .orElseThrow(() ->
                        new CourseException(
                                SavedCourseErrorCode.SAVED_COURSE_NOT_FOUND
                        )
                );

        if (!course.getMember().getId().equals(memberId)) {
            throw new CourseException(
                    SavedCourseErrorCode.SAVED_COURSE_ACCESS_DENIED
            );
        }

        RepresentativeMoodTag representativeMoodTag =
                getMoodTagByCourseId(List.of(courseId))
                        .get(courseId);

        List<DetailPlaceResponse> places =
                coursePlaceRepository
                        .findAllByCourseIdOrderByVisitOrderAsc(courseId)
                        .stream()
                        .map(this::toDetailPlaceResponse)
                        .toList();

        courseRepository.increaseViewCount(courseId);
        Integer viewCount = courseRepository.findViewCountById(courseId);

        return new DetailResponse(
                course.getId(),
                course.getTitle(),
                course.getCreatedAt().toLocalDate(),
                representativeMoodTag,
                toRepresentativePlaceCategory(course.getBasePlace()),
                course.getMemo(),
                places.size(),
                viewCount != null
                        ? viewCount
                        : 0,
                places
        );
    }

    private Map<Long, RepresentativeMoodTag> getMoodTagByCourseId(
            List<Long> courseIds
    ) {
        Map<Long, RepresentativeMoodTag> result =
                new LinkedHashMap<>();

        List<CourseMoodTag> courseMoodTags =
                courseMoodTagRepository
                        .findAllWithCourseAndMoodTagByCourseIds(
                                courseIds
                        );

        for (CourseMoodTag courseMoodTag : courseMoodTags) {
            Long courseId = courseMoodTag.getCourse().getId();
            MoodTag moodTag = courseMoodTag.getMoodTag();

            result.putIfAbsent(
                    courseId,
                    new RepresentativeMoodTag(
                            moodTag.getId(),
                            moodTag.getCode(),
                            moodTag.getName()
                    )
            );
        }

        return result;
    }

    private Map<Long, List<CoursePlace>> getCoursePlacesByCourseId(
            List<Long> courseIds
    ) {
        Map<Long, List<CoursePlace>> result = new LinkedHashMap<>();

        List<CoursePlace> coursePlaces =
                coursePlaceRepository
                        .findAllWithCourseAndPlaceByCourseIds(
                                courseIds
                        );

        for (CoursePlace coursePlace : coursePlaces) {
            Long courseId = coursePlace.getCourse().getId();

            result.computeIfAbsent(
                    courseId,
                    ignored -> new ArrayList<>()
            ).add(coursePlace);
        }

        return result;
    }

    private CardResponse toCardResponse(
            Course course,
            Map<Long, RepresentativeMoodTag> moodTagByCourseId,
            Map<Long, List<CoursePlace>> coursePlacesByCourseId
    ) {
        List<CoursePlace> coursePlaces =
                coursePlacesByCourseId.getOrDefault(
                        course.getId(),
                        List.of()
                );

        List<PreviewPlaceResponse> previewPlaces =
                coursePlaces.stream()
                        .filter(coursePlace ->
                                coursePlace.getPlaceRole()
                                        == PlaceRole.SELECTED
                        )
                        .limit(PREVIEW_PLACE_LIMIT)
                        .map(this::toPreviewPlaceResponse)
                        .toList();

        int placeCount = coursePlaces.size();

        int remainingPlaceCount = Math.max(
                placeCount - previewPlaces.size(),
                0
        );

        int viewCount = course.getViewCount() != null
                ? course.getViewCount()
                : 0;

        return new CardResponse(
                course.getId(),
                course.getTitle(),
                course.getCreatedAt().toLocalDate(),
                moodTagByCourseId.get(course.getId()),
                toRepresentativePlaceCategory(course.getBasePlace()),
                previewPlaces,
                remainingPlaceCount,
                placeCount,
                viewCount
        );
    }

    private DetailPlaceResponse toDetailPlaceResponse(
            CoursePlace coursePlace
    ) {
        Place place = coursePlace.getPlace();

        String name = hasText(coursePlace.getPlaceNameSnapshot())
                ? coursePlace.getPlaceNameSnapshot()
                : place.getName();

        String address = hasText(coursePlace.getAddressSnapshot())
                ? coursePlace.getAddressSnapshot()
                : place.getAddress();

        return new DetailPlaceResponse(
                coursePlace.getId(),
                place.getId(),
                coursePlace.getVisitOrder(),
                name,
                address,
                coursePlace.getMemo()
        );
    }

    private RepresentativeSubCategory toRepresentativePlaceCategory(
            Place basePlace
    ) {
        if (basePlace == null || !hasText(basePlace.getSubCategory())) {
            return null;
        }

        String subCategory = basePlace.getSubCategory();
        return new RepresentativeSubCategory(
                subCategory,
                subCategory
        );
    }

    private PreviewPlaceResponse toPreviewPlaceResponse(
            CoursePlace coursePlace
    ) {
        Place place = coursePlace.getPlace();

        String name = hasText(coursePlace.getPlaceNameSnapshot())
                ? coursePlace.getPlaceNameSnapshot()
                : place.getName();

        return new PreviewPlaceResponse(
                place.getId(),
                name,
                coursePlace.getVisitOrder()
        );
    }

    @Transactional
    public SavedCourseMemoUpdateResponse updateSavedCourseMemo(
            Long memberId,
            Long courseId,
            SavedCourseMemoUpdateRequest request
    ) {
        Course course = getEditableSavedCourse(
                memberId,
                courseId
        );

        String normalizedMemo = normalizeMemo(request.memo());

        course.updateMemo(normalizedMemo);

        return SavedCourseMemoUpdateResponse.of(
                course.getId(),
                normalizedMemo
        );
    }

    @Transactional
    public SavedCoursePlaceMemoUpdateResponse updateSavedCoursePlaceMemo(
            Long memberId,
            Long courseId,
            Long coursePlaceId,
            SavedCoursePlaceMemoUpdateRequest request
    ) {
        getEditableSavedCourse(
                memberId,
                courseId
        );

        CoursePlace coursePlace = coursePlaceRepository
                .findByIdAndCourseId(
                        coursePlaceId,
                        courseId
                )
                .orElseThrow(() ->
                        new CourseException(
                                SavedCourseErrorCode.SAVED_COURSE_PLACE_NOT_FOUND
                        )
                );

        String normalizedMemo = normalizeMemo(request.memo());

        coursePlace.updateMemo(normalizedMemo);

        return SavedCoursePlaceMemoUpdateResponse.of(
                courseId,
                coursePlace.getId(),
                normalizedMemo
        );
    }

    private Course getEditableSavedCourse(
            Long memberId,
            Long courseId
    ) {
        Course course = courseRepository
                .findSavedCourseDetailById(courseId)
                .orElseThrow(() ->
                        new CourseException(
                                SavedCourseErrorCode.SAVED_COURSE_NOT_FOUND
                        )
                );

        if (!course.getMember().getId().equals(memberId)) {
            throw new CourseException(
                    SavedCourseErrorCode.SAVED_COURSE_ACCESS_DENIED
            );
        }

        if (course.getSourceType() != CourseSourceType.USER_CREATED) {
            throw new CourseException(
                    SavedCourseErrorCode.SAVED_COURSE_ACCESS_DENIED
            );
        }

        return course;
    }

    private String normalizeMemo(String memo) {
        if (memo == null || memo.isBlank()) {
            return null;
        }

        return memo.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

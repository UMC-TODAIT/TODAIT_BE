package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.RepresentativeFoodCategoryResponse;
import com.example.TODAIT__BE.domain.course.dto.response.RepresentativeMoodTagResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseCardResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseOverviewResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCoursePreviewPlaceResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
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

@Service
public class SavedCourseService {

    private static final int COURSE_LIMIT = 2;
    private static final int PREVIEW_PLACE_LIMIT = 3;

    private final CourseRepository courseRepository;
    private final CourseMoodTagRepository courseMoodTagRepository;
    private final CourseFoodCategoryRepository courseFoodCategoryRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    public SavedCourseService(
            CourseRepository courseRepository,
            CourseMoodTagRepository courseMoodTagRepository,
            CourseFoodCategoryRepository courseFoodCategoryRepository,
            CoursePlaceRepository coursePlaceRepository
    ) {
        this.courseRepository = courseRepository;
        this.courseMoodTagRepository = courseMoodTagRepository;
        this.courseFoodCategoryRepository = courseFoodCategoryRepository;
        this.coursePlaceRepository = coursePlaceRepository;
    }

    @Transactional(readOnly = true)
    public SavedCourseOverviewResponse getSavedCourseOverview(Long memberId) {
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
            return new SavedCourseOverviewResponse(
                    List.of(),
                    List.of()
            );
        }

        List<Long> courseIds = new ArrayList<>(courseIdSet);

        Map<Long, RepresentativeMoodTagResponse> moodTagByCourseId =
                getMoodTagByCourseId(courseIds);

        Map<Long, RepresentativeFoodCategoryResponse> foodCategoryByCourseId =
                getFoodCategoryByCourseId(courseIds);

        Map<Long, List<CoursePlace>> coursePlacesByCourseId =
                getCoursePlacesByCourseId(courseIds);

        List<SavedCourseCardResponse> recentCourseResponses =
                recentCourses.stream()
                        .map(course ->
                                toCardResponse(
                                        course,
                                        moodTagByCourseId,
                                        foodCategoryByCourseId,
                                        coursePlacesByCourseId
                                )
                        )
                        .toList();

        List<SavedCourseCardResponse> popularCourseResponses =
                popularCourses.stream()
                        .map(course ->
                                toCardResponse(
                                        course,
                                        moodTagByCourseId,
                                        foodCategoryByCourseId,
                                        coursePlacesByCourseId
                                )
                        )
                        .toList();

        return new SavedCourseOverviewResponse(
                recentCourseResponses,
                popularCourseResponses
        );
    }

    private Map<Long, RepresentativeMoodTagResponse> getMoodTagByCourseId(
            List<Long> courseIds
    ) {
        Map<Long, RepresentativeMoodTagResponse> result =
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
                    new RepresentativeMoodTagResponse(
                            moodTag.getId(),
                            moodTag.getCode(),
                            moodTag.getName()
                    )
            );
        }

        return result;
    }

    private Map<Long, RepresentativeFoodCategoryResponse>
    getFoodCategoryByCourseId(
            List<Long> courseIds
    ) {
        Map<Long, RepresentativeFoodCategoryResponse> result =
                new LinkedHashMap<>();

        List<CourseFoodCategory> courseFoodCategories =
                courseFoodCategoryRepository
                        .findAllWithCourseAndFoodCategoryByCourseIds(
                                courseIds
                        );

        for (CourseFoodCategory courseFoodCategory : courseFoodCategories) {
            Long courseId = courseFoodCategory.getCourse().getId();
            FoodCategory foodCategory =
                    courseFoodCategory.getFoodCategory();

            result.putIfAbsent(
                    courseId,
                    new RepresentativeFoodCategoryResponse(
                            foodCategory.getId(),
                            foodCategory.getCode(),
                            foodCategory.getName()
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
                        .findAllWithCourseAndPlaceByCourseIdsAndPlaceRole(
                                courseIds,
                                PlaceRole.SELECTED
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

    private SavedCourseCardResponse toCardResponse(
            Course course,
            Map<Long, RepresentativeMoodTagResponse> moodTagByCourseId,
            Map<Long, RepresentativeFoodCategoryResponse> foodCategoryByCourseId,
            Map<Long, List<CoursePlace>> coursePlacesByCourseId
    ) {
        List<CoursePlace> coursePlaces =
                coursePlacesByCourseId.getOrDefault(
                        course.getId(),
                        List.of()
                );

        List<SavedCoursePreviewPlaceResponse> previewPlaces =
                coursePlaces.stream()
                        .limit(PREVIEW_PLACE_LIMIT)
                        .map(this::toPreviewPlaceResponse)
                        .toList();

        int placeCount = course.getPlaceCount() != null
                ? course.getPlaceCount()
                : 0;

        int remainingPlaceCount = Math.max(
                placeCount - previewPlaces.size(),
                0
        );

        int viewCount = course.getViewCount() != null
                ? course.getViewCount()
                : 0;

        return new SavedCourseCardResponse(
                course.getId(),
                course.getTitle(),
                course.getCreatedAt().toLocalDate(),
                moodTagByCourseId.get(course.getId()),
                foodCategoryByCourseId.get(course.getId()),
                previewPlaces,
                remainingPlaceCount,
                placeCount,
                viewCount
        );
    }

    private SavedCoursePreviewPlaceResponse toPreviewPlaceResponse(
            CoursePlace coursePlace
    ) {
        Place place = coursePlace.getPlace();

        String name = hasText(coursePlace.getPlaceNameSnapshot())
                ? coursePlace.getPlaceNameSnapshot()
                : place.getName();

        return new SavedCoursePreviewPlaceResponse(
                place.getId(),
                name,
                coursePlace.getVisitOrder()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

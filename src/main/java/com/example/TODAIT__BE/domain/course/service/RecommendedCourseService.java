package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseDetailResponse;
import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCoursePlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.RepresentativeMoodTagResponse;
import com.example.TODAIT__BE.domain.course.dto.response.RepresentativeSubCategoryResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceImage;
import com.example.TODAIT__BE.domain.place.repository.PlaceImageRepository;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendedCourseService {

    private static final Set<String> SUPPORTED_AREA_CODES =
            Set.of("HONGDAE", "YEONNAM", "SEONGSU");

    private final CourseRepository courseRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final CourseMoodTagRepository courseMoodTagRepository;
    private final PlaceImageRepository placeImageRepository;

    public RecommendedCourseService(
            CourseRepository courseRepository,
            CoursePlaceRepository coursePlaceRepository,
            CourseMoodTagRepository courseMoodTagRepository,
            PlaceImageRepository placeImageRepository
    ) {
        this.courseRepository = courseRepository;
        this.coursePlaceRepository = coursePlaceRepository;
        this.courseMoodTagRepository = courseMoodTagRepository;
        this.placeImageRepository = placeImageRepository;
    }

    @Transactional(readOnly = true)
    public RecommendedCourseDetailResponse getRecommendedCourseDetail(
            Long courseId
    ) {
        Course course = courseRepository
                .findByIdAndVisibilityAndSourceType(
                        courseId,
                        CourseVisibility.RECOMMENDED,
                        CourseSourceType.SERVICE_CREATED
                )
                .orElseThrow(() ->
                        new ProjectException(
                                CourseErrorCode.RECOMMENDED_COURSE_NOT_FOUND
                        )
                );

        validateSupportedArea(course);

        RepresentativeMoodTagResponse representativeMoodTag =
                getRepresentativeMoodTag(courseId);

        RepresentativeSubCategoryResponse representativePlaceCategory =
                getRepresentativeSubCategory(courseId);

        List<CoursePlace> coursePlaces =
                coursePlaceRepository
                        .findAllByCourseIdOrderByVisitOrderAsc(courseId);

        Map<Long, String> primaryImageUrlByPlaceId =
                getPrimaryImageUrlByPlaceId(coursePlaces);

        List<RecommendedCoursePlaceResponse> places =
                coursePlaces.stream()
                        .map(coursePlace ->
                                toPlaceResponse(
                                        coursePlace,
                                        primaryImageUrlByPlaceId
                                )
                        )
                        .toList();

        return new RecommendedCourseDetailResponse(
                course.getId(),
                course.getTitle(),
                representativeMoodTag,
                representativePlaceCategory,
                course.getPlaceCount(),
                places
        );
    }

    private void validateSupportedArea(Course course) {
        boolean isSupportedArea =
                course.getArea() != null
                        && Boolean.TRUE.equals(
                        course.getArea().getIsActive()
                )
                        && SUPPORTED_AREA_CODES.contains(
                        course.getArea().getCode()
                );

        if (!isSupportedArea) {
            throw new ProjectException(
                    CourseErrorCode.RECOMMENDED_COURSE_NOT_FOUND
            );
        }
    }

    private RepresentativeMoodTagResponse getRepresentativeMoodTag(
            Long courseId
    ) {
        return courseMoodTagRepository
                .findFirstByCourseIdOrderByIdAsc(courseId)
                .map(CourseMoodTag::getMoodTag)
                .map(this::toMoodTagResponse)
                .orElse(null);
    }

    private RepresentativeMoodTagResponse toMoodTagResponse(
            MoodTag moodTag
    ) {
        return new RepresentativeMoodTagResponse(
                moodTag.getId(),
                moodTag.getCode(),
                moodTag.getName()
        );
    }

    private RepresentativeSubCategoryResponse getRepresentativeSubCategory(
            Long courseId
    ) {
        return coursePlaceRepository
                .findFirstByCourseIdAndIsRepresentativeTrue(courseId)
                .map(CoursePlace::getPlace)
                .map(Place::getSubCategory)
                .filter(this::hasText)
                .map(subCategory ->
                        new RepresentativeSubCategoryResponse(
                                subCategory,
                                subCategory
                        )
                )
                .orElse(null);
    }

    private Map<Long, String> getPrimaryImageUrlByPlaceId(
            List<CoursePlace> coursePlaces
    ) {
        List<Long> placeIds = coursePlaces.stream()
                .map(CoursePlace::getPlace)
                .map(Place::getId)
                .toList();

        if (placeIds.isEmpty()) {
            return Map.of();
        }

        return placeImageRepository
                .findAllByPlace_IdInAndIsPrimaryTrueOrderByPlace_IdAscDisplayOrderAsc(
                        placeIds
                )
                .stream()
                .collect(Collectors.toMap(
                        placeImage -> placeImage.getPlace().getId(),
                        PlaceImage::getImageUrl,
                        (firstImageUrl, ignoredImageUrl) -> firstImageUrl
                ));
    }

    private RecommendedCoursePlaceResponse toPlaceResponse(
            CoursePlace coursePlace,
            Map<Long, String> primaryImageUrlByPlaceId
    ) {
        Place place = coursePlace.getPlace();

        String representativeImageUrl =
                primaryImageUrlByPlaceId.getOrDefault(
                        place.getId(),
                        place.getDefaultImageUrl()
                );

        String name = hasText(coursePlace.getPlaceNameSnapshot())
                ? coursePlace.getPlaceNameSnapshot()
                : place.getName();

        String address = hasText(coursePlace.getAddressSnapshot())
                ? coursePlace.getAddressSnapshot()
                : getPlaceAddress(place);

        Double latitude =
                coursePlace.getLatitudeSnapshot() != null
                        ? coursePlace.getLatitudeSnapshot()
                        : place.getLatitude();

        Double longitude =
                coursePlace.getLongitudeSnapshot() != null
                        ? coursePlace.getLongitudeSnapshot()
                        : place.getLongitude();

        return new RecommendedCoursePlaceResponse(
                coursePlace.getId(),
                place.getId(),
                coursePlace.getVisitOrder(),
                name,
                representativeImageUrl,
                address,
                latitude,
                longitude
        );
    }

    private String getPlaceAddress(Place place) {
        return hasText(place.getRoadAddress())
                ? place.getRoadAddress()
                : place.getAddress();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

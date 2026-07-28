package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseDetailResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.repository.PlaceImageRepository;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendedCourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CoursePlaceRepository coursePlaceRepository;
    @Mock
    private CourseMoodTagRepository courseMoodTagRepository;
    @Mock
    private PlaceImageRepository placeImageRepository;

    private RecommendedCourseService recommendedCourseService;

    @BeforeEach
    void setUp() {
        recommendedCourseService = new RecommendedCourseService(
                courseRepository,
                coursePlaceRepository,
                courseMoodTagRepository,
                placeImageRepository
        );
    }

    @Test
    void returnsRecommendedCourseDetailWithBaseAndSelectedPlaces() {
        Course course = mock(Course.class);
        given(course.getId()).willReturn(100L);
        given(course.getTitle()).willReturn("연남 데이트 코스");
        given(course.getPlaceCount()).willReturn(2);
        given(courseRepository.findActiveRecommendedCourseById(
                100L,
                CourseVisibility.RECOMMENDED,
                CourseSourceType.SERVICE_CREATED
        )).willReturn(Optional.of(course));

        MoodTag moodTag = mock(MoodTag.class);
        given(moodTag.getId()).willReturn(4L);
        given(moodTag.getCode()).willReturn("ROMANTIC");
        given(moodTag.getName()).willReturn("낭만적인");
        CourseMoodTag courseMoodTag = CourseMoodTag.builder()
                .moodTag(moodTag)
                .build();
        given(courseMoodTagRepository.findFirstByCourseIdOrderByIdAsc(100L))
                .willReturn(Optional.of(courseMoodTag));

        Place basePlace = place(21L, "base default image");
        given(basePlace.getSubCategory()).willReturn("감성 카페");
        Place selectedPlace = place(22L, "selected default image");
        CoursePlace baseCoursePlace = CoursePlace.builder()
                .id(101L)
                .place(basePlace)
                .visitOrder(1)
                .placeRole(PlaceRole.BASE)
                .isRepresentative(true)
                .placeNameSnapshot("스냅샷 기준 장소")
                .addressSnapshot("서울 마포구 기준로 1")
                .latitudeSnapshot(37.1)
                .longitudeSnapshot(126.1)
                .build();
        CoursePlace selectedCoursePlace = CoursePlace.builder()
                .id(102L)
                .place(selectedPlace)
                .visitOrder(2)
                .placeRole(PlaceRole.SELECTED)
                .placeNameSnapshot("스냅샷 선택 장소")
                .addressSnapshot("서울 마포구 선택로 2")
                .latitudeSnapshot(37.2)
                .longitudeSnapshot(126.2)
                .build();
        given(coursePlaceRepository.findAllByCourseIdOrderByVisitOrderAsc(100L))
                .willReturn(List.of(baseCoursePlace, selectedCoursePlace));
        PlaceImageRepository.PrimaryImageUrlView selectedPrimaryImage =
                primaryImage(22L, "selected primary image");
        given(placeImageRepository.findPrimaryImageUrlsByPlaceIds(List.of(21L, 22L)))
                .willReturn(List.of(selectedPrimaryImage));

        RecommendedCourseDetailResponse response =
                recommendedCourseService.getRecommendedCourseDetail(100L);

        assertThat(response.courseId()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("연남 데이트 코스");
        assertThat(response.representativeMoodTag().code()).isEqualTo("ROMANTIC");
        assertThat(response.representativePlaceCategory().name()).isEqualTo("감성 카페");
        assertThat(response.placeCount()).isEqualTo(2);
        assertThat(response.places()).hasSize(2);
        assertThat(response.places().get(0).coursePlaceId()).isEqualTo(101L);
        assertThat(response.places().get(0).visitOrder()).isEqualTo(1);
        assertThat(response.places().get(0).representativeImageUrl())
                .isEqualTo("base default image");
        assertThat(response.places().get(1).coursePlaceId()).isEqualTo(102L);
        assertThat(response.places().get(1).representativeImageUrl())
                .isEqualTo("selected primary image");
    }

    @Test
    void throwsWhenRecommendedCourseIsNotFound() {
        given(courseRepository.findActiveRecommendedCourseById(
                999L,
                CourseVisibility.RECOMMENDED,
                CourseSourceType.SERVICE_CREATED
        )).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                recommendedCourseService.getRecommendedCourseDetail(999L)
        )
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.RECOMMENDED_COURSE_NOT_FOUND);

        verify(coursePlaceRepository, never())
                .findAllByCourseIdOrderByVisitOrderAsc(999L);
    }

    private Place place(
            Long id,
            String defaultImageUrl
    ) {
        Place place = mock(Place.class);
        given(place.getId()).willReturn(id);
        given(place.getDefaultImageUrl()).willReturn(defaultImageUrl);
        return place;
    }

    private PlaceImageRepository.PrimaryImageUrlView primaryImage(
            Long placeId,
            String imageUrl
    ) {
        PlaceImageRepository.PrimaryImageUrlView view =
                mock(PlaceImageRepository.PrimaryImageUrlView.class);
        given(view.getPlaceId()).willReturn(placeId);
        given(view.getImageUrl()).willReturn(imageUrl);
        return view;
    }
}

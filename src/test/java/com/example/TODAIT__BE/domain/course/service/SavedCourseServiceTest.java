package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseOverviewResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SavedCourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseMoodTagRepository courseMoodTagRepository;
    @Mock
    private CoursePlaceRepository coursePlaceRepository;

    private SavedCourseService savedCourseService;

    @BeforeEach
    void setUp() {
        savedCourseService = new SavedCourseService(
                courseRepository,
                courseMoodTagRepository,
                coursePlaceRepository
        );
    }

    @Test
    void returnsEmptyListsWithoutLoadingRelatedData() {
        given(courseRepository.findRecentSavedCourses(eq(1L), any(Pageable.class)))
                .willReturn(List.of());
        given(courseRepository.findPopularSavedCourses(eq(1L), any(Pageable.class)))
                .willReturn(List.of());

        SavedCourseOverviewResponse response =
                savedCourseService.getSavedCourseOverview(1L);

        assertThat(response.recentCourses()).isEmpty();
        assertThat(response.popularCourses()).isEmpty();
        verify(courseMoodTagRepository, never())
                .findAllWithCourseAndMoodTagByCourseIds(any());
        verify(coursePlaceRepository, never())
                .findAllWithCourseAndPlaceByCourseIds(any());
    }

    @Test
    void returnsRecentAndPopularCoursesWithBasePlaceCategory() {
        Course recentOnly = course(
                1L,
                "최근 코스",
                "감성 카페",
                3
        );
        Course sharedCourse = course(
                2L,
                "최근이자 인기 코스",
                "전시",
                10
        );
        Course popularOnly = course(
                3L,
                "인기 코스",
                "공방",
                15
        );

        given(courseRepository.findRecentSavedCourses(eq(1L), any(Pageable.class)))
                .willReturn(List.of(recentOnly, sharedCourse));
        given(courseRepository.findPopularSavedCourses(eq(1L), any(Pageable.class)))
                .willReturn(List.of(sharedCourse, popularOnly));

        List<CourseMoodTag> courseMoodTags = List.of(
                courseMoodTag(recentOnly, 10L, "CALM", "차분한"),
                courseMoodTag(sharedCourse, 20L, "ROMANTIC", "로맨틱"),
                courseMoodTag(popularOnly, 30L, "ACTIVE", "활발한")
        );
        List<CoursePlace> coursePlaces = List.of(
                coursePlace(recentOnly, 100L, "기준 장소", 1, PlaceRole.BASE),
                coursePlace(recentOnly, 101L, "장소 1", 2),
                coursePlace(recentOnly, 102L, "장소 2", 3),
                coursePlace(recentOnly, 103L, "장소 3", 4),
                countOnlyCoursePlace(recentOnly, 5, PlaceRole.SELECTED),
                coursePlace(sharedCourse, 200L, "공유 기준 장소", 1, PlaceRole.BASE),
                coursePlace(sharedCourse, 201L, "공유 장소", 2),
                coursePlace(popularOnly, 300L, "인기 기준 장소", 1, PlaceRole.BASE),
                coursePlace(popularOnly, 301L, "인기 장소", 2)
        );

        given(courseMoodTagRepository.findAllWithCourseAndMoodTagByCourseIds(
                List.of(1L, 2L, 3L)
        )).willReturn(courseMoodTags);
        given(coursePlaceRepository.findAllWithCourseAndPlaceByCourseIds(
                List.of(1L, 2L, 3L)
        )).willReturn(coursePlaces);

        SavedCourseOverviewResponse response =
                savedCourseService.getSavedCourseOverview(1L);

        assertThat(response.recentCourses()).hasSize(2);
        assertThat(response.popularCourses()).hasSize(2);
        assertThat(response.recentCourses().get(0).courseId()).isEqualTo(1L);
        assertThat(response.popularCourses().get(0).courseId()).isEqualTo(2L);
        assertThat(response.recentCourses().get(0).representativePlaceCategory().name())
                .isEqualTo("감성 카페");
        assertThat(response.recentCourses().get(1).representativePlaceCategory().name())
                .isEqualTo("전시");
        assertThat(response.popularCourses().get(1).representativePlaceCategory().name())
                .isEqualTo("공방");
        assertThat(response.recentCourses().get(0).previewPlaces()).hasSize(3);
        assertThat(response.recentCourses().get(0).previewPlaces())
                .extracting("placeId")
                .containsExactly(101L, 102L, 103L);
        assertThat(response.recentCourses().get(0).placeCount())
                .isEqualTo(5);
        assertThat(response.recentCourses().get(0).remainingPlaceCount())
                .isEqualTo(2);
        assertThat(response.recentCourses().get(0).representativeMoodTag().code())
                .isEqualTo("CALM");

        verify(courseMoodTagRepository)
                .findAllWithCourseAndMoodTagByCourseIds(
                        List.of(1L, 2L, 3L)
                );
    }

    private Course course(
            Long id,
            String title,
            String subCategory,
            Integer viewCount
    ) {
        Place basePlace = mock(Place.class);
        given(basePlace.getSubCategory()).willReturn(subCategory);

        Course course = mock(Course.class);
        given(course.getId()).willReturn(id);
        given(course.getTitle()).willReturn(title);
        given(course.getCreatedAt()).willReturn(LocalDateTime.of(
                2026,
                7,
                29,
                12,
                0
        ));
        given(course.getBasePlace()).willReturn(basePlace);
        given(course.getViewCount()).willReturn(viewCount);
        return course;
    }

    private CourseMoodTag courseMoodTag(
            Course course,
            Long moodTagId,
            String moodTagCode,
            String moodTagName
    ) {
        MoodTag moodTag = mock(MoodTag.class);
        given(moodTag.getId()).willReturn(moodTagId);
        given(moodTag.getCode()).willReturn(moodTagCode);
        given(moodTag.getName()).willReturn(moodTagName);

        return CourseMoodTag.builder()
                .course(course)
                .moodTag(moodTag)
                .build();
    }

    private CoursePlace coursePlace(
            Course course,
            Long placeId,
            String placeName,
            Integer visitOrder
    ) {
        return coursePlace(
                course,
                placeId,
                placeName,
                visitOrder,
                PlaceRole.SELECTED
        );
    }

    private CoursePlace coursePlace(
            Course course,
            Long placeId,
            String placeName,
            Integer visitOrder,
            PlaceRole placeRole
    ) {
        Place place = mock(Place.class);
        if (placeRole == PlaceRole.SELECTED) {
            given(place.getId()).willReturn(placeId);
        }

        return CoursePlace.builder()
                .course(course)
                .place(place)
                .visitOrder(visitOrder)
                .placeRole(placeRole)
                .placeNameSnapshot(placeName)
                .build();
    }

    private CoursePlace countOnlyCoursePlace(
            Course course,
            Integer visitOrder,
            PlaceRole placeRole
    ) {
        return CoursePlace.builder()
                .course(course)
                .place(mock(Place.class))
                .visitOrder(visitOrder)
                .placeRole(placeRole)
                .placeNameSnapshot("count only")
                .build();
    }
}

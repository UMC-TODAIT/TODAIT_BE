package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendedCourseSaveServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CoursePlaceRepository coursePlaceRepository;
    @Mock
    private CourseMoodTagRepository courseMoodTagRepository;
    @Mock
    private CourseFoodCategoryRepository courseFoodCategoryRepository;
    @Mock
    private MemberRepository memberRepository;

    private RecommendedCourseSaveService recommendedCourseSaveService;

    @BeforeEach
    void setUp() {
        recommendedCourseSaveService = new RecommendedCourseSaveService(
                courseRepository,
                coursePlaceRepository,
                courseMoodTagRepository,
                courseFoodCategoryRepository,
                memberRepository
        );
    }

    @Test
    void savesRecommendedCourseWhenSourceCourseIsSavable() {
        Place basePlace = place(100L);
        Place selectedPlace = place(200L);
        Course sourceCourse = sourceCourse(basePlace);
        Member member = Member.builder().id(1L).build();

        givenSourceCourse(sourceCourse);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(coursePlaceRepository.findAllByCourseIdOrderByVisitOrderAsc(10L))
                .willReturn(List.of(
                        coursePlace(basePlace, PlaceRole.BASE, 1),
                        coursePlace(selectedPlace, PlaceRole.SELECTED, 2)
                ));
        given(courseMoodTagRepository.findAllByCourseIdOrderByIdAsc(10L))
                .willReturn(List.of(courseMoodTag()));
        given(courseFoodCategoryRepository.findAllByCourseIdOrderByIdAsc(10L))
                .willReturn(List.of(courseFoodCategory()));
        given(courseRepository.save(any(Course.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        RecommendedCourseSaveResponse response =
                recommendedCourseSaveService.saveRecommendedCourse(10L, 1L);

        assertThat(response.sourceCourseId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("recommended");
        assertThat(response.visibility()).isEqualTo(CourseVisibility.PRIVATE);
        assertThat(response.sourceType()).isEqualTo(CourseSourceType.USER_CREATED);
        assertThat(response.placeCount()).isEqualTo(2);

        ArgumentCaptor<Course> courseCaptor =
                ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        Course savedCourse = courseCaptor.getValue();
        assertThat(savedCourse.getMember()).isEqualTo(member);
        assertThat(savedCourse.getBasePlace()).isEqualTo(basePlace);
        assertThat(savedCourse.getVisibility()).isEqualTo(CourseVisibility.PRIVATE);
        assertThat(savedCourse.getSourceType()).isEqualTo(CourseSourceType.USER_CREATED);

        verify(coursePlaceRepository).saveAll(any());
        verify(courseMoodTagRepository).saveAll(any());
        verify(courseFoodCategoryRepository).saveAll(any());
    }

    @Test
    void throwsWhenSourceCourseHasNoBasePlace() {
        Course sourceCourse = sourceCourse(null);

        assertRecommendedCourseNotSavable(
                sourceCourse,
                List.of(coursePlace(place(200L), PlaceRole.SELECTED, 1))
        );
    }

    @Test
    void throwsWhenSourceCourseHasNoBaseCoursePlace() {
        Course sourceCourse = sourceCourse(place(100L));

        assertRecommendedCourseNotSavable(
                sourceCourse,
                List.of(coursePlace(place(200L), PlaceRole.SELECTED, 1))
        );
    }

    @Test
    void throwsWhenSourceCourseHasMultipleBaseCoursePlaces() {
        Place basePlace = place(100L);
        Course sourceCourse = sourceCourse(basePlace);

        assertRecommendedCourseNotSavable(
                sourceCourse,
                List.of(
                        coursePlace(basePlace, PlaceRole.BASE, 1),
                        coursePlace(place(101L), PlaceRole.BASE, 2),
                        coursePlace(place(200L), PlaceRole.SELECTED, 3)
                )
        );
    }

    @Test
    void throwsWhenSourceCourseHasNoSelectedCoursePlace() {
        Place basePlace = place(100L);
        Course sourceCourse = sourceCourse(basePlace);

        assertRecommendedCourseNotSavable(
                sourceCourse,
                List.of(coursePlace(basePlace, PlaceRole.BASE, 1))
        );
    }

    @Test
    void throwsWhenSourceCourseVisitOrderIsNotContinuous() {
        Place basePlace = place(100L);
        Course sourceCourse = sourceCourse(basePlace);

        assertRecommendedCourseNotSavable(
                sourceCourse,
                List.of(
                        coursePlace(basePlace, PlaceRole.BASE, 1),
                        coursePlace(place(200L), PlaceRole.SELECTED, 3)
                )
        );
    }

    @Test
    void throwsWhenSourceCourseMoodTagsAreEmpty() {
        Place basePlace = place(100L);
        Course sourceCourse = sourceCourse(basePlace);

        givenSavableSourcePlaces(sourceCourse, basePlace);
        given(courseMoodTagRepository.findAllByCourseIdOrderByIdAsc(10L))
                .willReturn(List.of());
        given(courseFoodCategoryRepository.findAllByCourseIdOrderByIdAsc(10L))
                .willReturn(List.of(courseFoodCategory()));

        assertThatThrownBy(() ->
                recommendedCourseSaveService.saveRecommendedCourse(10L, 1L))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.RECOMMENDED_COURSE_NOT_SAVABLE);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenSourceCourseFoodCategoriesAreEmpty() {
        Place basePlace = place(100L);
        Course sourceCourse = sourceCourse(basePlace);

        givenSavableSourcePlaces(sourceCourse, basePlace);
        given(courseMoodTagRepository.findAllByCourseIdOrderByIdAsc(10L))
                .willReturn(List.of(courseMoodTag()));
        given(courseFoodCategoryRepository.findAllByCourseIdOrderByIdAsc(10L))
                .willReturn(List.of());

        assertThatThrownBy(() ->
                recommendedCourseSaveService.saveRecommendedCourse(10L, 1L))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.RECOMMENDED_COURSE_NOT_SAVABLE);

        verify(courseRepository, never()).save(any());
    }

    private void assertRecommendedCourseNotSavable(
            Course sourceCourse,
            List<CoursePlace> sourcePlaces
    ) {
        givenSourceCourse(sourceCourse);
        given(memberRepository.findById(1L))
                .willReturn(Optional.of(Member.builder().id(1L).build()));
        given(coursePlaceRepository.findAllByCourseIdOrderByVisitOrderAsc(10L))
                .willReturn(sourcePlaces);

        assertThatThrownBy(() ->
                recommendedCourseSaveService.saveRecommendedCourse(10L, 1L))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.RECOMMENDED_COURSE_NOT_SAVABLE);

        verify(courseRepository, never()).save(any());
    }

    private void givenSavableSourcePlaces(Course sourceCourse, Place basePlace) {
        givenSourceCourse(sourceCourse);
        given(memberRepository.findById(1L))
                .willReturn(Optional.of(Member.builder().id(1L).build()));
        List<CoursePlace> sourcePlaces = List.of(
                coursePlace(basePlace, PlaceRole.BASE, 1),
                coursePlace(place(200L), PlaceRole.SELECTED, 2)
        );
        given(coursePlaceRepository.findAllByCourseIdOrderByVisitOrderAsc(10L))
                .willReturn(sourcePlaces);
    }

    private void givenSourceCourse(Course sourceCourse) {
        given(courseRepository.findActiveRecommendedCourseById(
                10L,
                CourseVisibility.RECOMMENDED,
                CourseSourceType.SERVICE_CREATED
        )).willReturn(Optional.of(sourceCourse));
    }

    private Course sourceCourse(Place basePlace) {
        return Course.builder()
                .id(10L)
                .basePlace(basePlace)
                .area(mock(Area.class))
                .title("recommended")
                .memo("memo")
                .visibility(CourseVisibility.RECOMMENDED)
                .sourceType(CourseSourceType.SERVICE_CREATED)
                .build();
    }

    private CoursePlace coursePlace(
            Place place,
            PlaceRole placeRole,
            Integer visitOrder
    ) {
        return CoursePlace.builder()
                .place(place)
                .placeRole(placeRole)
                .visitOrder(visitOrder)
                .isRepresentative(false)
                .placeNameSnapshot("place")
                .addressSnapshot("address")
                .latitudeSnapshot(37.1)
                .longitudeSnapshot(127.1)
                .categorySnapshot("CAFE")
                .build();
    }

    private Place place(Long id) {
        Place place = mock(Place.class);
        lenient().when(place.getId()).thenReturn(id);
        return place;
    }

    private CourseMoodTag courseMoodTag() {
        return CourseMoodTag.builder()
                .moodTag(mock(MoodTag.class))
                .build();
    }

    private CourseFoodCategory courseFoodCategory() {
        return CourseFoodCategory.builder()
                .foodCategory(mock(FoodCategory.class))
                .build();
    }
}

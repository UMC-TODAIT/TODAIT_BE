package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("recommended course save response placeCount is calculated from source course places")
    void saveRecommendedCourseUsesSourceCoursePlaceCount() {
        Member member = Member.builder()
                .id(1L)
                .nickname("member")
                .build();
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        Area area = mock(Area.class);
        Place basePlace = mock(Place.class);
        given(basePlace.getId()).willReturn(100L);
        Place selectedPlace = mock(Place.class);

        Course sourceCourse = Course.builder()
                .id(10L)
                .member(Member.builder().id(9L).nickname("admin").build())
                .basePlace(basePlace)
                .area(area)
                .title("추천 코스")
                .memo("추천 메모")
                .visibility(CourseVisibility.RECOMMENDED)
                .sourceType(CourseSourceType.SERVICE_CREATED)
                .build();
        given(courseRepository.findActiveRecommendedCourseById(
                10L,
                CourseVisibility.RECOMMENDED,
                CourseSourceType.SERVICE_CREATED
        )).willReturn(Optional.of(sourceCourse));

        List<CoursePlace> sourcePlaces = List.of(
                CoursePlace.builder()
                        .course(sourceCourse)
                        .place(basePlace)
                        .visitOrder(1)
                        .placeRole(PlaceRole.BASE)
                        .placeNameSnapshot("base")
                        .addressSnapshot("base address")
                        .build(),
                CoursePlace.builder()
                        .course(sourceCourse)
                        .place(selectedPlace)
                        .visitOrder(2)
                        .placeRole(PlaceRole.SELECTED)
                        .placeNameSnapshot("selected")
                        .addressSnapshot("selected address")
                        .build()
        );
        given(coursePlaceRepository.findAllByCourseIdOrderByVisitOrderAsc(10L))
                .willReturn(sourcePlaces);
        given(courseMoodTagRepository.findAllByCourseIdOrderByIdAsc(10L))
                .willReturn(List.of());
        given(courseFoodCategoryRepository.findAllByCourseIdOrderByIdAsc(10L))
                .willReturn(List.of());
        given(courseRepository.save(any(Course.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(coursePlaceRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(courseMoodTagRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(courseFoodCategoryRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));

        RecommendedCourseSaveResponse response =
                recommendedCourseSaveService.saveRecommendedCourse(10L, 1L);

        assertThat(response.sourceCourseId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("추천 코스");
        assertThat(response.placeCount()).isEqualTo(2);
        assertThat(response.visibility()).isEqualTo(CourseVisibility.PRIVATE);
        assertThat(response.sourceType()).isEqualTo(CourseSourceType.USER_CREATED);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        Course savedCourse = courseCaptor.getValue();
        assertThat(savedCourse.getMember()).isEqualTo(member);
        assertThat(savedCourse.getBasePlace()).isEqualTo(basePlace);
        assertThat(savedCourse.getArea()).isEqualTo(area);

        verify(coursePlaceRepository).saveAll(anyList());
        verify(courseMoodTagRepository).saveAll(anyList());
        verify(courseFoodCategoryRepository).saveAll(anyList());
    }
}

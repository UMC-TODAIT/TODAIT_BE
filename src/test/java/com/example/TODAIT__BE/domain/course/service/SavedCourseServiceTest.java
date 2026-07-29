package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseDetailResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseOverviewResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
    void returnsSavedCourseDetailWithOrderedSnapshotPlacesAndIncrementedViewCount() {
        Course course = detailCourse(
                100L,
                1L,
                "성수 데이트",
                "전체 메모",
                "전시"
        );
        List<CourseMoodTag> courseMoodTags = List.of(
                courseMoodTag(course, 10L, "ROMANTIC", "로맨틱")
        );
        List<CoursePlace> coursePlaces = List.of(
                detailCoursePlace(course, 1000L, 2, "스냅샷 장소 A", "스냅샷 주소 A"),
                detailCoursePlace(course, 1001L, 3, "스냅샷 장소 B", "스냅샷 주소 B")
        );

        given(courseRepository.findSavedCourseDetailById(100L))
                .willReturn(Optional.of(course));
        given(courseMoodTagRepository.findAllWithCourseAndMoodTagByCourseIds(
                List.of(100L)
        )).willReturn(courseMoodTags);
        given(coursePlaceRepository.findAllByCourseIdOrderByVisitOrderAsc(100L))
                .willReturn(coursePlaces);
        given(courseRepository.increaseViewCount(100L))
                .willReturn(1);
        given(courseRepository.findViewCountById(100L))
                .willReturn(6);

        SavedCourseDetailResponse response =
                savedCourseService.getSavedCourseDetail(1L, 100L);

        assertThat(response.courseId()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("성수 데이트");
        assertThat(response.memo()).isEqualTo("전체 메모");
        assertThat(response.representativeMoodTag().code())
                .isEqualTo("ROMANTIC");
        assertThat(response.representativePlaceCategory().name())
                .isEqualTo("전시");
        assertThat(response.placeCount()).isEqualTo(2);
        assertThat(response.viewCount()).isEqualTo(6);
        assertThat(response.places())
                .extracting("visitOrder")
                .containsExactly(2, 3);
        assertThat(response.places())
                .extracting("name")
                .containsExactly("스냅샷 장소 A", "스냅샷 장소 B");
        assertThat(response.places())
                .extracting("address")
                .containsExactly("스냅샷 주소 A", "스냅샷 주소 B");

        InOrder inOrder = inOrder(courseRepository);
        inOrder.verify(courseRepository).increaseViewCount(100L);
        inOrder.verify(courseRepository).findViewCountById(100L);
    }

    @Test
    void throwsNotFoundWhenSavedCourseIsDeletedOrMissing() {
        given(courseRepository.findSavedCourseDetailById(100L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                savedCourseService.getSavedCourseDetail(1L, 100L)
        )
                .isInstanceOfSatisfying(
                        CourseException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(CourseErrorCode.SAVED_COURSE_NOT_FOUND)
                );

        verify(courseMoodTagRepository, never())
                .findAllWithCourseAndMoodTagByCourseIds(any());
        verify(coursePlaceRepository, never())
                .findAllByCourseIdOrderByVisitOrderAsc(any());
        verify(courseRepository, never()).increaseViewCount(any());
    }

    @Test
    void throwsAccessDeniedWhenCourseBelongsToAnotherMember() {
        Course course = courseOwnedBy(2L);

        given(courseRepository.findSavedCourseDetailById(100L))
                .willReturn(Optional.of(course));

        assertThatThrownBy(() ->
                savedCourseService.getSavedCourseDetail(1L, 100L)
        )
                .isInstanceOfSatisfying(
                        CourseException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(CourseErrorCode.SAVED_COURSE_ACCESS_DENIED)
                );

        verify(courseMoodTagRepository, never())
                .findAllWithCourseAndMoodTagByCourseIds(any());
        verify(coursePlaceRepository, never())
                .findAllByCourseIdOrderByVisitOrderAsc(any());
        verify(courseRepository, never()).increaseViewCount(any());
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

    private Course detailCourse(
            Long id,
            Long memberId,
            String title,
            String memo,
            String subCategory
    ) {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(memberId);

        Place basePlace = mock(Place.class);
        given(basePlace.getSubCategory()).willReturn(subCategory);

        Course course = mock(Course.class);
        given(course.getId()).willReturn(id);
        given(course.getMember()).willReturn(member);
        given(course.getTitle()).willReturn(title);
        given(course.getMemo()).willReturn(memo);
        given(course.getCreatedAt()).willReturn(LocalDateTime.of(
                2026,
                7,
                29,
                12,
                0
        ));
        given(course.getBasePlace()).willReturn(basePlace);
        return course;
    }

    private Course courseOwnedBy(Long memberId) {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(memberId);

        Course course = mock(Course.class);
        given(course.getMember()).willReturn(member);
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

    private CoursePlace detailCoursePlace(
            Course course,
            Long placeId,
            Integer visitOrder,
            String placeNameSnapshot,
            String addressSnapshot
    ) {
        Place place = mock(Place.class);
        given(place.getId()).willReturn(placeId);

        return CoursePlace.builder()
                .course(course)
                .place(place)
                .visitOrder(visitOrder)
                .placeRole(PlaceRole.SELECTED)
                .placeNameSnapshot(placeNameSnapshot)
                .addressSnapshot(addressSnapshot)
                .build();
    }
}

package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.TODAIT__BE.domain.course.dto.request.SavedCourseMemoUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.SavedCoursePlaceMemoUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseMemoUpdateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCoursePlaceMemoUpdateResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.code.SavedCourseErrorCode;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavedCourseMemoUpdateServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long COURSE_ID = 10L;
    private static final Long COURSE_PLACE_ID = 25L;

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
    void updatesSavedCourseMemo() {
        Course course = course(
                COURSE_ID,
                MEMBER_ID,
                CourseSourceType.USER_CREATED
        );

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        SavedCourseMemoUpdateRequest request =
                new SavedCourseMemoUpdateRequest(
                        "  힐링하고 싶은 날 즐기는 데이트 코스  "
                );

        SavedCourseMemoUpdateResponse response =
                savedCourseService.updateSavedCourseMemo(
                        MEMBER_ID,
                        COURSE_ID,
                        request
                );

        assertThat(response.courseId()).isEqualTo(COURSE_ID);
        assertThat(response.memo())
                .isEqualTo("힐링하고 싶은 날 즐기는 데이트 코스");
        assertThat(course.getMemo())
                .isEqualTo("힐링하고 싶은 날 즐기는 데이트 코스");
    }

    @Test
    void convertsBlankCourseMemoToNull() {
        Course course = course(
                COURSE_ID,
                MEMBER_ID,
                CourseSourceType.USER_CREATED
        );

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        SavedCourseMemoUpdateRequest request =
                new SavedCourseMemoUpdateRequest("   ");

        SavedCourseMemoUpdateResponse response =
                savedCourseService.updateSavedCourseMemo(
                        MEMBER_ID,
                        COURSE_ID,
                        request
                );

        assertThat(response.memo()).isNull();
        assertThat(course.getMemo()).isNull();
    }

    @Test
    void throwsAccessDeniedWhenUpdatingOtherUsersCourseMemo() {
        Course course = course(
                COURSE_ID,
                999L,
                CourseSourceType.USER_CREATED
        );

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        SavedCourseMemoUpdateRequest request =
                new SavedCourseMemoUpdateRequest("수정 메모");

        assertThatThrownBy(() ->
                savedCourseService.updateSavedCourseMemo(
                        MEMBER_ID,
                        COURSE_ID,
                        request
                )
        )
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(SavedCourseErrorCode.SAVED_COURSE_ACCESS_DENIED);

        assertThat(course.getMemo()).isNull();
    }

    @Test
    void throwsAccessDeniedWhenUpdatingServiceCreatedCourseMemo() {
        Course course = course(
                COURSE_ID,
                MEMBER_ID,
                CourseSourceType.SERVICE_CREATED
        );

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        SavedCourseMemoUpdateRequest request =
                new SavedCourseMemoUpdateRequest("수정 메모");

        assertThatThrownBy(() ->
                savedCourseService.updateSavedCourseMemo(
                        MEMBER_ID,
                        COURSE_ID,
                        request
                )
        )
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(SavedCourseErrorCode.SAVED_COURSE_ACCESS_DENIED);

        assertThat(course.getMemo()).isNull();
    }

    @Test
    void updatesSavedCoursePlaceMemo() {
        Course course = course(
                COURSE_ID,
                MEMBER_ID,
                CourseSourceType.USER_CREATED
        );

        CoursePlace coursePlace =
                coursePlace(COURSE_PLACE_ID, course);

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        given(coursePlaceRepository.findByIdAndCourseId(
                COURSE_PLACE_ID,
                COURSE_ID
        )).willReturn(Optional.of(coursePlace));

        SavedCoursePlaceMemoUpdateRequest request =
                new SavedCoursePlaceMemoUpdateRequest(
                        "  조용하고 풍경이 예쁨  "
                );

        SavedCoursePlaceMemoUpdateResponse response =
                savedCourseService.updateSavedCoursePlaceMemo(
                        MEMBER_ID,
                        COURSE_ID,
                        COURSE_PLACE_ID,
                        request
                );

        assertThat(response.courseId()).isEqualTo(COURSE_ID);
        assertThat(response.coursePlaceId()).isEqualTo(COURSE_PLACE_ID);
        assertThat(response.memo()).isEqualTo("조용하고 풍경이 예쁨");
        assertThat(coursePlace.getMemo()).isEqualTo("조용하고 풍경이 예쁨");
    }

    @Test
    void convertsBlankCoursePlaceMemoToNull() {
        Course course = course(
                COURSE_ID,
                MEMBER_ID,
                CourseSourceType.USER_CREATED
        );

        CoursePlace coursePlace =
                coursePlace(COURSE_PLACE_ID, course);

        coursePlace.updateMemo("기존 장소 메모");

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        given(coursePlaceRepository.findByIdAndCourseId(
                COURSE_PLACE_ID,
                COURSE_ID
        )).willReturn(Optional.of(coursePlace));

        SavedCoursePlaceMemoUpdateRequest request =
                new SavedCoursePlaceMemoUpdateRequest("");

        SavedCoursePlaceMemoUpdateResponse response =
                savedCourseService.updateSavedCoursePlaceMemo(
                        MEMBER_ID,
                        COURSE_ID,
                        COURSE_PLACE_ID,
                        request
                );

        assertThat(response.memo()).isNull();
        assertThat(coursePlace.getMemo()).isNull();
    }

    @Test
    void throwsNotFoundWhenCoursePlaceDoesNotExist() {
        Course course = course(
                COURSE_ID,
                MEMBER_ID,
                CourseSourceType.USER_CREATED
        );

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        given(coursePlaceRepository.findByIdAndCourseId(
                COURSE_PLACE_ID,
                COURSE_ID
        )).willReturn(Optional.empty());

        SavedCoursePlaceMemoUpdateRequest request =
                new SavedCoursePlaceMemoUpdateRequest("장소 메모");

        assertThatThrownBy(() ->
                savedCourseService.updateSavedCoursePlaceMemo(
                        MEMBER_ID,
                        COURSE_ID,
                        COURSE_PLACE_ID,
                        request
                )
        )
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(
                        SavedCourseErrorCode.SAVED_COURSE_PLACE_NOT_FOUND
                );
    }

    @Test
    void throwsNotFoundWhenCoursePlaceBelongsToDifferentCourse() {
        Course course = course(
                COURSE_ID,
                MEMBER_ID,
                CourseSourceType.USER_CREATED
        );

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        /*
         * findByIdAndCourseId()는 두 조건을 동시에 만족해야 하므로
         * coursePlaceId가 존재하더라도 다른 course 소속이면
         * Optional.empty()가 반환되는 상황을 나타냅니다.
         */
        given(coursePlaceRepository.findByIdAndCourseId(
                COURSE_PLACE_ID,
                COURSE_ID
        )).willReturn(Optional.empty());

        SavedCoursePlaceMemoUpdateRequest request =
                new SavedCoursePlaceMemoUpdateRequest("장소 메모");

        assertThatThrownBy(() ->
                savedCourseService.updateSavedCoursePlaceMemo(
                        MEMBER_ID,
                        COURSE_ID,
                        COURSE_PLACE_ID,
                        request
                )
        )
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(
                        SavedCourseErrorCode.SAVED_COURSE_PLACE_NOT_FOUND
                );
    }

    private Course course(
            Long courseId,
            Long memberId,
            CourseSourceType sourceType
    ) {
        Member member = Member.builder()
                .id(memberId)
                .build();

        return Course.builder()
                .id(courseId)
                .member(member)
                .basePlace(mock(Place.class))
                .area(mock(Area.class))
                .title("테스트 저장 코스")
                .visibility(CourseVisibility.PRIVATE)
                .sourceType(sourceType)
                .build();
    }

    private CoursePlace coursePlace(
            Long coursePlaceId,
            Course course
    ) {
        return CoursePlace.builder()
                .id(coursePlaceId)
                .course(course)
                .place(mock(Place.class))
                .visitOrder(1)
                .placeRole(PlaceRole.BASE)
                .isRepresentative(true)
                .placeNameSnapshot("테스트 장소")
                .addressSnapshot("서울 테스트 주소")
                .build();
    }
}

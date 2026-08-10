package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseDeleteResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.code.SavedCourseErrorCode;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
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
class SavedCourseDeleteServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long COURSE_ID = 10L;

    @Mock
    private CourseRepository courseRepository;

    private SavedCourseDeleteService savedCourseDeleteService;

    @BeforeEach
    void setUp() {
        savedCourseDeleteService =
                new SavedCourseDeleteService(courseRepository);
    }

    @Test
    void deletesOwnUserCreatedCourse() {
        Course course = course(
                COURSE_ID,
                MEMBER_ID,
                CourseSourceType.USER_CREATED
        );

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        SavedCourseDeleteResponse response =
                savedCourseDeleteService.deleteSavedCourse(
                        COURSE_ID,
                        MEMBER_ID
                );

        assertThat(response.courseId()).isEqualTo(COURSE_ID);
        assertThat(course.getDeletedAt()).isNotNull();
    }

    @Test
    void throwsNotFoundWhenCourseDoesNotExist() {
        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                savedCourseDeleteService.deleteSavedCourse(
                        COURSE_ID,
                        MEMBER_ID
                )
        )
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(SavedCourseErrorCode.SAVED_COURSE_NOT_FOUND);
    }

    @Test
    void throwsAccessDeniedWhenRequesterIsNotOwner() {
        Course course = course(
                COURSE_ID,
                999L,
                CourseSourceType.USER_CREATED
        );

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        assertThatThrownBy(() ->
                savedCourseDeleteService.deleteSavedCourse(
                        COURSE_ID,
                        MEMBER_ID
                )
        )
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(SavedCourseErrorCode.SAVED_COURSE_ACCESS_DENIED);

        assertThat(course.getDeletedAt()).isNull();
    }

    @Test
    void throwsAccessDeniedWhenCourseIsServiceCreated() {
        Course course = course(
                COURSE_ID,
                MEMBER_ID,
                CourseSourceType.SERVICE_CREATED
        );

        given(courseRepository.findSavedCourseDetailById(COURSE_ID))
                .willReturn(Optional.of(course));

        assertThatThrownBy(() ->
                savedCourseDeleteService.deleteSavedCourse(
                        COURSE_ID,
                        MEMBER_ID
                )
        )
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(SavedCourseErrorCode.SAVED_COURSE_ACCESS_DENIED);

        assertThat(course.getDeletedAt()).isNull();
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
                .basePlace(org.mockito.Mockito.mock(Place.class))
                .area(org.mockito.Mockito.mock(Area.class))
                .title("테스트 저장 코스")
                .visibility(CourseVisibility.PRIVATE)
                .sourceType(sourceType)
                .build();
    }
}

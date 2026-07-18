package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseSaveServiceTest {

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;
    @Mock
    private MoodTagRepository moodTagRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseMoodTagRepository courseMoodTagRepository;
    @Mock
    private CourseFoodCategoryRepository courseFoodCategoryRepository;
    @Mock
    private CoursePlaceRepository coursePlaceRepository;

    private CourseSaveService courseSaveService;

    @BeforeEach
    void setUp() {
        courseSaveService = new CourseSaveService(
                courseDraftRepository,
                courseDraftFoodCategoryRepository,
                courseDraftPlaceRepository,
                moodTagRepository,
                courseRepository,
                courseMoodTagRepository,
                courseFoodCategoryRepository,
                coursePlaceRepository
        );
    }

    private Member member(Long id) {
        return Member.builder().id(id).build();
    }

    private CourseDraft courseDraft(Long id, Member owner, CourseDraftStatus status) {
        return CourseDraft.builder()
                .id(id)
                .member(owner)
                .status(status)
                .build();
    }

    @Test
    void throwsWhenRequesterIsNotOwner() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모", List.of(1L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 2L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.NOT_COURSE_DRAFT_OWNER);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseDraftAlreadyCompleted() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.COMPLETED);
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모", List.of(1L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.COURSE_DRAFT_ALREADY_COMPLETED);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenMoodTagCountExceedsMax() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest(
                "제목", "메모", List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L)
        );

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_MOOD_TAG_COUNT);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenBasePlaceMissing() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        MoodTag moodTag = mock(MoodTag.class);
        given(moodTagRepository.findAllById(List.of(1L))).willReturn(List.of(moodTag));

        CourseDraftFoodCategory draftFoodCategory = CourseDraftFoodCategory.builder().build();
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft)).willReturn(List.of(draftFoodCategory));

        CourseDraftPlace selectedOnly = CourseDraftPlace.builder()
                .placeRole(PlaceRole.SELECTED)
                .visitOrder(2)
                .place(mock(Place.class))
                .build();
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(selectedOnly));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모", List.of(1L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_BASE_PLACE);

        verify(courseRepository, never()).save(any());
    }
}

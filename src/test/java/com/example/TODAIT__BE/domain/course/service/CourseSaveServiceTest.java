package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseSaveServiceTest {

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    @Mock
    private CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;
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
                courseDraftMoodTagRepository,
                courseDraftPlaceRepository,
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
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    @Test
    void throwsWhenRequesterIsNotOwner() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모");

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 2L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseDraftAlreadyCompleted() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.COMPLETED);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모");

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.COURSE_DRAFT_ALREADY_COMPLETED);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseDraftAbandoned() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ABANDONED);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모");

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_COURSE_DRAFT_STATUS);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseDraftIsNotOrdering() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.FOOD_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모");

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_COURSE_DRAFT_STATUS);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseDraftExpired() {
        CourseDraft draft = CourseDraft.builder()
                .id(10L)
                .member(member(1L))
                .status(CourseDraftStatus.ORDERING)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모");

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_COURSE_DRAFT_STATUS);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseTitleBlank() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest(" ", "메모");

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_COURSE_TITLE);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenMoodTagCountExceedsMax() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모");
        List<CourseDraftMoodTag> draftMoodTags = List.of(
                CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build(),
                CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build(),
                CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build(),
                CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build(),
                CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build(),
                CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build(),
                CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build()
        );
        given(courseDraftMoodTagRepository.findByCourseDraft(draft)).willReturn(draftMoodTags);

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_MOOD_TAG_COUNT);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenFoodCategoryNotSelected() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        given(courseDraftMoodTagRepository.findByCourseDraft(draft))
                .willReturn(List.of(CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build()));
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft)).willReturn(List.of());

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모");

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.FOOD_CATEGORY_NOT_SELECTED);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenBasePlaceMissing() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        given(courseDraftMoodTagRepository.findByCourseDraft(draft))
                .willReturn(List.of(CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build()));

        CourseDraftFoodCategory draftFoodCategory = CourseDraftFoodCategory.builder().build();
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft)).willReturn(List.of(draftFoodCategory));

        CourseDraftPlace selectedOnly = CourseDraftPlace.builder()
                .placeRole(PlaceRole.SELECTED)
                .visitOrder(2)
                .place(mock(Place.class))
                .build();
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(selectedOnly));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모");

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_BASE_PLACE);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenSelectedPlaceMissing() {
        Place basePlace = mock(Place.class);
        given(basePlace.getId()).willReturn(100L);

        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        given(courseDraftMoodTagRepository.findByCourseDraft(draft))
                .willReturn(List.of(CourseDraftMoodTag.builder().moodTag(mock(MoodTag.class)).build()));

        CourseDraftFoodCategory draftFoodCategory = CourseDraftFoodCategory.builder().build();
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft)).willReturn(List.of(draftFoodCategory));

        CourseDraftPlace baseOnly = CourseDraftPlace.builder()
                .placeRole(PlaceRole.BASE)
                .visitOrder(1)
                .place(basePlace)
                .build();
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(baseOnly));

        CourseSaveRequest request = new CourseSaveRequest("제목", "메모");

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_SELECTED_PLACE);

        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("valid draft is saved as a course with snapshots and completed status")
    void saveCourseSuccess() {
        Member owner = member(1L);
        Area area = mock(Area.class);

        PlaceCategory placeCategory = mock(PlaceCategory.class);
        given(placeCategory.getCode()).willReturn("CAFE");

        Place basePlace = mock(Place.class);
        given(basePlace.getId()).willReturn(100L);
        given(basePlace.getArea()).willReturn(area);
        given(basePlace.getName()).willReturn("base");
        given(basePlace.getAddress()).willReturn("base address");
        given(basePlace.getLatitude()).willReturn(37.1);
        given(basePlace.getLongitude()).willReturn(127.1);
        given(basePlace.getPlaceCategory()).willReturn(placeCategory);

        Place selectedPlace = mock(Place.class);
        given(selectedPlace.getId()).willReturn(200L);
        given(selectedPlace.getName()).willReturn("selected");
        given(selectedPlace.getAddress()).willReturn("selected address");
        given(selectedPlace.getLatitude()).willReturn(37.2);
        given(selectedPlace.getLongitude()).willReturn(127.2);
        given(selectedPlace.getPlaceCategory()).willReturn(placeCategory);

        CourseDraft draft = courseDraft(10L, owner, CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        MoodTag moodTag = mock(MoodTag.class);
        given(moodTag.getId()).willReturn(1L);
        given(moodTag.getCode()).willReturn("CALM");
        given(moodTag.getName()).willReturn("차분한");
        CourseDraftMoodTag draftMoodTag = CourseDraftMoodTag.builder()
                .courseDraft(draft)
                .moodTag(moodTag)
                .build();
        given(courseDraftMoodTagRepository.findByCourseDraft(draft)).willReturn(List.of(draftMoodTag));

        FoodCategory foodCategory = mock(FoodCategory.class);
        given(foodCategory.getId()).willReturn(5L);
        given(foodCategory.getCode()).willReturn("KOREAN");
        given(foodCategory.getName()).willReturn("한식");
        CourseDraftFoodCategory draftFoodCategory = CourseDraftFoodCategory.builder()
                .courseDraft(draft)
                .foodCategory(foodCategory)
                .build();
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft)).willReturn(List.of(draftFoodCategory));

        CourseDraftPlace baseDraftPlace = CourseDraftPlace.builder()
                .courseDraft(draft)
                .place(basePlace)
                .visitOrder(1)
                .placeRole(PlaceRole.BASE)
                .build();
        CourseDraftPlace selectedDraftPlace = CourseDraftPlace.builder()
                .courseDraft(draft)
                .place(selectedPlace)
                .visitOrder(2)
                .placeRole(PlaceRole.SELECTED)
                .memo("place memo")
                .build();
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(baseDraftPlace, selectedDraftPlace));

        given(courseRepository.save(any(Course.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(courseMoodTagRepository.save(any(CourseMoodTag.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(courseFoodCategoryRepository.save(any(CourseFoodCategory.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(coursePlaceRepository.save(any(CoursePlace.class))).willAnswer(invocation -> invocation.getArgument(0));

        CourseSaveRequest request = new CourseSaveRequest("course title", "course memo");

        CourseSaveResponse response = courseSaveService.saveCourse(10L, 1L, request);

        assertThat(response.title()).isEqualTo("course title");
        assertThat(response.memo()).isEqualTo("course memo");
        assertThat(response.placeCount()).isEqualTo(2);
        assertThat(response.moodTags()).hasSize(1);
        assertThat(response.foodCategories()).hasSize(1);
        assertThat(response.places()).hasSize(2);
        assertThat(response.places().get(0).placeRole()).isEqualTo(PlaceRole.BASE);
        assertThat(response.places().get(1).memo()).isEqualTo("place memo");
        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.COMPLETED);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        Course savedCourse = courseCaptor.getValue();
        assertThat(savedCourse.getMember()).isEqualTo(owner);
        assertThat(savedCourse.getBasePlace()).isEqualTo(basePlace);
        assertThat(savedCourse.getArea()).isEqualTo(area);
        assertThat(savedCourse.getPlaceCount()).isEqualTo(2);

        verify(courseMoodTagRepository).save(any(CourseMoodTag.class));
        verify(courseFoodCategoryRepository).save(any(CourseFoodCategory.class));
        verify(coursePlaceRepository, times(2)).save(any(CoursePlace.class));
        verify(courseDraftRepository).save(draft);
    }
}

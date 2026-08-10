package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest.SaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse.SaveResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.code.CourseSaveErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.course.service.support.CourseSaveSupport;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.code.FoodCategoryErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.code.MoodTagErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CourseSaveServiceTest {

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
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
    @Mock
    private MoodTagRepository moodTagRepository;

    private CourseSaveService courseSaveService;

    @BeforeEach
    void setUp() {
        courseSaveService = new CourseSaveService(
                courseDraftRepository,
                courseDraftFoodCategoryRepository,
                courseDraftPlaceRepository,
                courseRepository,
                moodTagRepository,
                new CourseDraftValidator(),
                new CourseSaveSupport(
                        courseMoodTagRepository,
                        courseFoodCategoryRepository,
                        coursePlaceRepository
                )
        );
    }

    private Member member(Long id) {
        return Member.builder().id(id).build();
    }

    private CourseDraft courseDraft(
            Long id,
            Member owner,
            CourseDraftStatus status
    ) {
        return CourseDraft.builder()
                .id(id)
                .member(owner)
                .status(status)
                .build();
    }

    private MoodTag moodTag(Long id, String code, String name) {
        MoodTag moodTag = mock(MoodTag.class);
        lenient().when(moodTag.getId()).thenReturn(id);
        lenient().when(moodTag.getCode()).thenReturn(code);
        lenient().when(moodTag.getName()).thenReturn(name);
        return moodTag;
    }

    private FoodCategory activeFoodCategory() {
        FoodCategory foodCategory = mock(FoodCategory.class);
        given(foodCategory.getIsActive()).willReturn(true);
        return foodCategory;
    }

    @Test
    void throwsWhenRequesterIsNotOwner() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 2L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseDraftAlreadyCompletedWithoutLinkedCourse() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.COMPLETED);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .satisfies(exception -> {
                    CourseException courseException = (CourseException) exception;
                    assertThat(courseException.getErrorCode()).isEqualTo(CourseSaveErrorCode.COURSE_DRAFT_ALREADY_COMPLETED);
                    assertThat((Map<String, Object>) courseException.getResult()).containsEntry("courseId", null);
                });

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseDraftAlreadyCompletedWithLinkedCourseIncludesCourseId() {
        Course existingCourse = mock(Course.class);
        given(existingCourse.getId()).willReturn(999L);

        CourseDraft draft = CourseDraft.builder()
                .id(10L)
                .member(member(1L))
                .status(CourseDraftStatus.COMPLETED)
                .course(existingCourse)
                .build();
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .satisfies(exception -> {
                    CourseException courseException = (CourseException) exception;
                    assertThat(courseException.getErrorCode()).isEqualTo(CourseSaveErrorCode.COURSE_DRAFT_ALREADY_COMPLETED);
                    assertThat((Map<String, Object>) courseException.getResult()).containsEntry("courseId", 999L);
                });

        verify(courseRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(
            value = CourseDraftStatus.class,
            names = {"COMPLETED", "SAVING"},
            mode = EnumSource.Mode.EXCLUDE
    )
    void throwsConflictWhenCourseDraftIsNotSavable(CourseDraftStatus status) {
        CourseDraft draft = courseDraft(10L, member(1L), status);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .satisfies(errorCode -> {
                    assertThat(errorCode).isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
                    assertThat(((CourseDraftErrorCode) errorCode).getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseTitleBlank() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest(" ", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseSaveErrorCode.INVALID_COURSE_TITLE);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenCourseTitleExceedsMaxLength() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest("a".repeat(256), "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseSaveErrorCode.INVALID_COURSE_TITLE);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenMoodTagIdsIsNull() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest("제목", "메모", null);

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_MOOD_TAG_COUNT);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenMoodTagCountIsLessThanMin() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_MOOD_TAG_COUNT);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenMoodTagCountExceedsMax() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_MOOD_TAG_COUNT);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenMoodTagIdsHaveDuplicate() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 1L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_MOOD_TAG_COUNT);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenMoodTagDoesNotExist() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        MoodTag hip = moodTag(1L, "HIP", "힙한");
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L))).willReturn(List.of(hip));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(TaxonomyException.class)
                .extracting("errorCode")
                .isEqualTo(MoodTagErrorCode.MOOD_TAG_NOT_FOUND);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenMoodTagIsInactive() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        MoodTag hip = moodTag(1L, "HIP", "힙한");
        // id 2 exists but is inactive, so the active-only query excludes it from the result
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L))).willReturn(List.of(hip));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(TaxonomyException.class)
                .extracting("errorCode")
                .isEqualTo(MoodTagErrorCode.MOOD_TAG_NOT_FOUND);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenFoodCategoryNotSelected() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        MoodTag hip = moodTag(1L, "HIP", "힙한");
        MoodTag calm = moodTag(2L, "CALM", "차분한");
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L))).willReturn(List.of(hip, calm));
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft)).willReturn(List.of());

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseSaveErrorCode.FOOD_CATEGORY_NOT_SELECTED);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenDraftFoodCategoryIsInactive() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        MoodTag hip = moodTag(1L, "HIP", "힙한");
        MoodTag calm = moodTag(2L, "CALM", "차분한");
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L))).willReturn(List.of(hip, calm));

        FoodCategory inactiveFoodCategory = mock(FoodCategory.class);
        given(inactiveFoodCategory.getIsActive()).willReturn(false);
        CourseDraftFoodCategory draftFoodCategory = CourseDraftFoodCategory.builder()
                .foodCategory(inactiveFoodCategory)
                .build();
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft)).willReturn(List.of(draftFoodCategory));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(TaxonomyException.class)
                .extracting("errorCode")
                .isEqualTo(FoodCategoryErrorCode.FOOD_CATEGORY_NOT_FOUND);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenBasePlaceMissing() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        MoodTag hip = moodTag(1L, "HIP", "힙한");
        MoodTag calm = moodTag(2L, "CALM", "차분한");
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L))).willReturn(List.of(hip, calm));

        CourseDraftFoodCategory draftFoodCategory = CourseDraftFoodCategory.builder()
                .foodCategory(activeFoodCategory())
                .build();
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft)).willReturn(List.of(draftFoodCategory));

        CourseDraftPlace selectedOnly = CourseDraftPlace.builder()
                .placeRole(PlaceRole.SELECTED)
                .visitOrder(2)
                .place(mock(Place.class))
                .build();
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(selectedOnly));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_BASE_PLACE);

        verify(courseRepository, never()).save(any());
    }

    @Test
    void throwsWhenSelectedPlaceMissing() {
        Place basePlace = mock(Place.class);
        given(basePlace.getId()).willReturn(100L);

        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        MoodTag hip = moodTag(1L, "HIP", "힙한");
        MoodTag calm = moodTag(2L, "CALM", "차분한");
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L))).willReturn(List.of(hip, calm));

        CourseDraftFoodCategory draftFoodCategory = CourseDraftFoodCategory.builder()
                .foodCategory(activeFoodCategory())
                .build();
        given(courseDraftFoodCategoryRepository.findByCourseDraft(draft)).willReturn(List.of(draftFoodCategory));

        CourseDraftPlace baseOnly = CourseDraftPlace.builder()
                .placeRole(PlaceRole.BASE)
                .visitOrder(1)
                .place(basePlace)
                .build();
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(baseOnly));

        SaveRequest request = new SaveRequest("제목", "메모", List.of(1L, 2L));

        assertThatThrownBy(() -> courseSaveService.saveCourse(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_SELECTED_PLACE);

        verify(courseRepository, never()).save(any());
    }

    @Test
    @DisplayName("valid draft is saved as a course with snapshots, completed status, and mood tags in request order")
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

        CourseDraft draft = courseDraft(10L, owner, CourseDraftStatus.SAVING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        MoodTag romantic = moodTag(4L, "ROMANTIC", "로맨틱");
        MoodTag hip = moodTag(1L, "HIP", "힙한");
        // request order is [4, 1] — response must preserve this order, not natural id order
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(4L, 1L))).willReturn(List.of(hip, romantic));

        FoodCategory foodCategory = mock(FoodCategory.class);
        given(foodCategory.getId()).willReturn(5L);
        given(foodCategory.getCode()).willReturn("KOREAN");
        given(foodCategory.getName()).willReturn("한식");
        given(foodCategory.getIsActive()).willReturn(true);
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

        SaveRequest request = new SaveRequest("  course title  ", "  ", List.of(4L, 1L));

        SaveResponse response = courseSaveService.saveCourse(10L, 1L, request);

        assertThat(response.title()).isEqualTo("course title");
        assertThat(response.memo()).isNull();
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.COMPLETED);
        assertThat(response.savedAt()).isNull();
        assertThat(response.placeCount()).isEqualTo(2);
        assertThat(response.moodTags()).hasSize(2);
        assertThat(response.moodTags().get(0).moodTagId()).isEqualTo(4L);
        assertThat(response.moodTags().get(1).moodTagId()).isEqualTo(1L);
        assertThat(response.foodCategories()).hasSize(1);
        assertThat(response.places()).hasSize(2);
        assertThat(response.places().get(0).placeRole()).isEqualTo(PlaceRole.BASE);
        assertThat(response.places().get(1).memo()).isEqualTo("place memo");
        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.COMPLETED);
        assertThat(draft.getCourse()).isNotNull();

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        Course savedCourse = courseCaptor.getValue();
        assertThat(savedCourse.getMember()).isEqualTo(owner);
        assertThat(savedCourse.getBasePlace()).isEqualTo(basePlace);
        assertThat(savedCourse.getArea()).isEqualTo(area);

        verify(courseMoodTagRepository, times(2)).save(any(CourseMoodTag.class));
        verify(courseFoodCategoryRepository).save(any(CourseFoodCategory.class));
        verify(coursePlaceRepository, times(2)).save(any(CoursePlace.class));
        verify(courseDraftRepository).save(draft);
    }
}

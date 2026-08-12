package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CurrentResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseDraftCurrentServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long DRAFT_ID = 10L;

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    @Mock
    private CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;

    private CourseDraftService service;

    @BeforeEach
    void setUp() {
        service = new CourseDraftService(
                courseDraftRepository,
                null,
                courseDraftMoodTagRepository,
                courseDraftFoodCategoryRepository,
                courseDraftPlaceRepository,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new CourseDraftValidator(),
                new com.example.TODAIT__BE.domain.course.config.CourseDraftProperties(30, "0 0 3 * * *", 500, 20),
                Clock.systemDefaultZone()
        );
    }

    @Test
    void returnsNullWhenCurrentDraftDoesNotExist() {
        given(courseDraftRepository.findFirstByMemberIdAndStatusInOrderByUpdatedAtDescIdDesc(
                MEMBER_ID,
                progressStatuses()
        )).willReturn(Optional.empty());

        CurrentResponse response = service.getCurrentCourseDraft(MEMBER_ID);

        assertThat(response).isNull();
    }

    @Test
    void returnsLatestProgressDraftWithSelectionsForScreenRecovery() {
        CourseDraft draft = CourseDraft.builder()
                .id(DRAFT_ID)
                .member(Member.builder().id(MEMBER_ID).build())
                .status(CourseDraftStatus.ORDERING)
                .build();
        MoodTag moodTag = moodTag(2L, "CALM", "차분한");
        FoodCategory foodCategory = foodCategory(3L, "KOREAN", "한식");
        CourseDraftPlace basePlace = CourseDraftPlace.builder()
                .id(100L)
                .courseDraft(draft)
                .place(place(20L))
                .placeRole(PlaceRole.BASE)
                .visitOrder(1)
                .build();

        given(courseDraftRepository.findFirstByMemberIdAndStatusInOrderByUpdatedAtDescIdDesc(
                MEMBER_ID,
                progressStatuses()
        )).willReturn(Optional.of(draft));
        given(courseDraftMoodTagRepository.findByCourseDraftOrderByIdAsc(draft))
                .willReturn(List.of(CourseDraftMoodTag.builder()
                        .courseDraft(draft)
                        .moodTag(moodTag)
                        .build()));
        given(courseDraftFoodCategoryRepository.findByCourseDraftOrderByIdAsc(draft))
                .willReturn(List.of(CourseDraftFoodCategory.builder()
                        .courseDraft(draft)
                        .foodCategory(foodCategory)
                        .build()));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(basePlace));

        CurrentResponse response = service.getCurrentCourseDraft(MEMBER_ID);

        assertThat(response.courseDraftId()).isEqualTo(DRAFT_ID);
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.ORDERING);
        assertThat(response.moodTags()).extracting("moodTagId").containsExactly(2L);
        assertThat(response.foodCategories()).extracting("foodCategoryId").containsExactly(3L);
        assertThat(response.places()).hasSize(1);
        assertThat(response.places().get(0).placeRole()).isEqualTo(PlaceRole.BASE);
        assertThat(response.places().get(0).area().code()).isEqualTo("YEONNAM");
        assertThat(response.places().get(0).category().code()).isEqualTo("CAFE");
    }

    @Test
    void excludesCompletedAndAbandonedDraftsFromCurrentLookup() {
        given(courseDraftRepository.findFirstByMemberIdAndStatusInOrderByUpdatedAtDescIdDesc(
                MEMBER_ID,
                progressStatuses()
        )).willReturn(Optional.empty());

        service.getCurrentCourseDraft(MEMBER_ID);

        verify(courseDraftRepository).findFirstByMemberIdAndStatusInOrderByUpdatedAtDescIdDesc(
                MEMBER_ID,
                progressStatuses()
        );
    }

    private List<CourseDraftStatus> progressStatuses() {
        return List.of(
                CourseDraftStatus.MOOD_SELECTING,
                CourseDraftStatus.FOOD_SELECTING,
                CourseDraftStatus.BASE_PLACE_SELECTING,
                CourseDraftStatus.PLACE_SELECTING,
                CourseDraftStatus.ORDERING,
                CourseDraftStatus.SAVING
        );
    }

    private Place place(Long id) {
        return Place.builder()
                .id(id)
                .area(area(1L, "YEONNAM", "연남"))
                .placeCategory(placeCategory(2L, "CAFE", "카페"))
                .name("연남 카페")
                .address("서울 마포구")
                .roadAddress("서울 마포구")
                .latitude(37.56)
                .longitude(126.92)
                .defaultImageUrl("https://example.com/image.jpg")
                .subCategory("디저트")
                .build();
    }

    private MoodTag moodTag(Long id, String code, String name) {
        MoodTag moodTag = mock(MoodTag.class);
        lenient().when(moodTag.getId()).thenReturn(id);
        lenient().when(moodTag.getCode()).thenReturn(code);
        lenient().when(moodTag.getName()).thenReturn(name);
        return moodTag;
    }

    private FoodCategory foodCategory(Long id, String code, String name) {
        FoodCategory foodCategory = mock(FoodCategory.class);
        lenient().when(foodCategory.getId()).thenReturn(id);
        lenient().when(foodCategory.getCode()).thenReturn(code);
        lenient().when(foodCategory.getName()).thenReturn(name);
        return foodCategory;
    }

    private Area area(Long id, String code, String name) {
        Area area = mock(Area.class);
        lenient().when(area.getId()).thenReturn(id);
        lenient().when(area.getCode()).thenReturn(code);
        lenient().when(area.getName()).thenReturn(name);
        return area;
    }

    private PlaceCategory placeCategory(Long id, String code, String name) {
        PlaceCategory placeCategory = mock(PlaceCategory.class);
        lenient().when(placeCategory.getId()).thenReturn(id);
        lenient().when(placeCategory.getCode()).thenReturn(code);
        lenient().when(placeCategory.getName()).thenReturn(name);
        return placeCategory;
    }
}

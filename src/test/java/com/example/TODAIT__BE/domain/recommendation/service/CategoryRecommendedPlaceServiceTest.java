package com.example.TODAIT__BE.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.dto.response.CategoryRecommendedPlaceResponse;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationResultRepository;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceCandidateData;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceCandidateLoader;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceRankingPolicy;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceReasonResolver;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceRecommendationSelection;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceResponseAssembler;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.repository.FoodCategoryRepository;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import com.example.TODAIT__BE.domain.recommendation.service.support.EvaluatedNearBasePlace;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CategoryRecommendedPlaceServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long COURSE_DRAFT_ID = 5L;

    @Mock
    private CourseDraftRepository courseDraftRepository;

    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;

    @Mock
    private CourseDraftMoodTagRepository courseDraftMoodTagRepository;

    @Mock
    private CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;

    @Mock
    private PlaceCategoryRepository placeCategoryRepository;

    @Mock
    private FoodCategoryRepository foodCategoryRepository;

    @Mock
    private NearBasePlaceCandidateLoader candidateLoader;

    @Mock
    private NearBasePlaceRankingPolicy rankingPolicy;

    @Mock
    private RecommendationLogRepository recommendationLogRepository;

    @Mock
    private RecommendationResultRepository recommendationResultRepository;

    @Mock
    private NearBasePlaceReasonResolver reasonResolver;

    @Mock
    private NearBasePlaceResponseAssembler responseAssembler;

    private CategoryRecommendedPlaceService service;

    @BeforeEach
    void setUp() {
        service = new CategoryRecommendedPlaceService(
                courseDraftRepository,
                courseDraftPlaceRepository,
                courseDraftMoodTagRepository,
                courseDraftFoodCategoryRepository,
                placeCategoryRepository,
                foodCategoryRepository,
                candidateLoader,
                rankingPolicy,
                recommendationLogRepository,
                recommendationResultRepository,
                new ObjectMapper(),
                reasonResolver,
                responseAssembler
        );
    }

    @Test
    void throwsInvalidPlaceSizeWhenSizeIsOutOfRange() {
        assertThatThrownBy(() ->
                service.getRecommendedPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        "CAFE",
                        0
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        RecommendationErrorCode.INVALID_PLACE_SIZE
                                )
                );

        assertThatThrownBy(() ->
                service.getRecommendedPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        "CAFE",
                        21
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        RecommendationErrorCode.INVALID_PLACE_SIZE
                                )
                );

        verify(courseDraftRepository, never()).findById(any());
        verify(recommendationLogRepository, never()).save(any());
    }

    @Test
    void throwsCourseDraftNotFoundWhenDraftDoesNotExist() {
        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.getRecommendedPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        "CAFE",
                        10
                )
        )
                .isInstanceOfSatisfying(
                        CourseException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        CourseErrorCode.COURSE_DRAFT_NOT_FOUND
                                )
                );

        verify(recommendationLogRepository, never()).save(any());
    }

    @Test
    void throwsAccessDeniedWhenDraftBelongsToAnotherMember() {
        CourseDraft draft = mock(CourseDraft.class);
        Member member = mock(Member.class);

        given(member.getId()).willReturn(999L);
        given(draft.getMember()).willReturn(member);

        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() ->
                service.getRecommendedPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        "CAFE",
                        10
                )
        )
                .isInstanceOfSatisfying(
                        CourseException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED
                                )
                );

        verify(recommendationLogRepository, never()).save(any());
    }

    @Test
    void throwsStatusConflictWhenDraftIsNotPlaceSelecting() {
        CourseDraft draft =
                draft(CourseDraftStatus.BASE_PLACE_SELECTING);

        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() ->
                service.getRecommendedPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        "CAFE",
                        10
                )
        )
                .isInstanceOfSatisfying(
                        CourseException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        CourseErrorCode.COURSE_DRAFT_STATUS_CONFLICT
                                )
                );

        verify(recommendationLogRepository, never()).save(any());
    }

    @Test
    void throwsBasePlaceConflictWhenBasePlaceDoesNotExist() {
        CourseDraft draft =
                draft(CourseDraftStatus.PLACE_SELECTING);

        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.of(draft));

        given(courseDraftPlaceRepository
                .findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of());

        assertThatThrownBy(() ->
                service.getRecommendedPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        "CAFE",
                        10
                )
        )
                .isInstanceOfSatisfying(
                        CourseException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        CourseErrorCode
                                                .COURSE_DRAFT_BASE_PLACE_CONFLICT
                                )
                );

        verify(recommendationLogRepository, never()).save(any());
    }

    @Test
    void savesRecommendationLogAndSkipsResultsWhenSelectionIsEmpty() {
        CourseDraft draft =
                draft(CourseDraftStatus.PLACE_SELECTING);

        Place basePlace = basePlace();
        CourseDraftPlace baseDraftPlace =
                baseDraftPlace(basePlace);

        PlaceCategory category = mock(PlaceCategory.class);
        given(category.getId()).willReturn(1L);
        given(category.getCode()).willReturn("CAFE");
        given(category.getName()).willReturn("카페");
        given(category.getIsActive()).willReturn(true);

        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.of(draft));

        given(courseDraftPlaceRepository
                .findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(baseDraftPlace));

        given(placeCategoryRepository.findByCode("CAFE"))
                .willReturn(Optional.of(category));

        given(courseDraftMoodTagRepository
                .findMoodTagIdsByCourseDraftId(COURSE_DRAFT_ID))
                .willReturn(List.of(1L, 2L));

        given(courseDraftFoodCategoryRepository
                .findFoodCategoryIdsByCourseDraftId(COURSE_DRAFT_ID))
                .willReturn(List.of(1L));

        NearBasePlaceCandidateData candidateData =
                new NearBasePlaceCandidateData(
                        List.of(),
                        java.util.Map.of(),
                        java.util.Map.of()
                );

        given(candidateLoader.load(
                anyList(),
                eq("CAFE"),
                anySet()
        )).willReturn(candidateData);

        NearBasePlaceRecommendationSelection emptySelection =
                new NearBasePlaceRecommendationSelection(
                        List.of(),
                        4
                );

        given(rankingPolicy.evaluateAndSelect(
                eq(candidateData),
                eq(basePlace),
                anySet(),
                anySet(),
                any(),
                eq("CAFE"),
                anyInt()
        )).willReturn(emptySelection);

        RecommendationLog savedLog =
                mock(RecommendationLog.class);

        given(recommendationLogRepository.save(any(RecommendationLog.class)))
                .willReturn(savedLog);

        CategoryRecommendedPlaceResponse response =
                mock(CategoryRecommendedPlaceResponse.class);

        given(responseAssembler.assemble(
                savedLog,
                basePlace,
                emptySelection
        )).willReturn(response);

        CategoryRecommendedPlaceResponse result =
                service.getRecommendedPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        "CAFE",
                        10
                );

        assertThat(result).isSameAs(response);

        verify(recommendationLogRepository)
                .save(any(RecommendationLog.class));

        verify(recommendationResultRepository, never())
                .saveAll(any());
    }

    private CourseDraft draft(CourseDraftStatus status) {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(MEMBER_ID);

        CourseDraft draft = mock(CourseDraft.class);
        given(draft.getId()).willReturn(COURSE_DRAFT_ID);
        given(draft.getMember()).willReturn(member);
        given(draft.getStatus()).willReturn(status);

        return draft;
    }

    private Place basePlace() {
        Area area = mock(Area.class);
        given(area.getId()).willReturn(1L);
        given(area.getCode()).willReturn("HONGDAE");
        given(area.getName()).willReturn("홍대");

        Place place = mock(Place.class);
        given(place.getId()).willReturn(1L);
        given(place.getName()).willReturn("홍대 테스트 카페");
        given(place.getArea()).willReturn(area);

        return place;
    }

    private CourseDraftPlace baseDraftPlace(Place basePlace) {
        CourseDraftPlace draftPlace =
                mock(CourseDraftPlace.class);

        given(draftPlace.getPlaceRole())
                .willReturn(PlaceRole.BASE);

        given(draftPlace.getVisitOrder())
                .willReturn(1);

        given(draftPlace.getPlace())
                .willReturn(basePlace);

        return draftPlace;
    }

    @Test
    void savesRecommendationResultsWithSequentialRanksWhenPlacesAreSelected() {
        CourseDraft draft =
                draft(CourseDraftStatus.PLACE_SELECTING);

        Place basePlace = basePlace();
        CourseDraftPlace baseDraftPlace =
                baseDraftPlace(basePlace);

        PlaceCategory category = mock(PlaceCategory.class);
        given(category.getId()).willReturn(1L);
        given(category.getCode()).willReturn("CAFE");
        given(category.getName()).willReturn("카페");
        given(category.getIsActive()).willReturn(true);

        Place firstPlace = mock(Place.class);
        given(firstPlace.getId()).willReturn(2L);

        Place secondPlace = mock(Place.class);
        given(secondPlace.getId()).willReturn(3L);

        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.of(draft));

        given(courseDraftPlaceRepository
                .findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(baseDraftPlace));

        given(placeCategoryRepository.findByCode("CAFE"))
                .willReturn(Optional.of(category));

        given(courseDraftMoodTagRepository
                .findMoodTagIdsByCourseDraftId(COURSE_DRAFT_ID))
                .willReturn(List.of(1L, 2L));

        given(courseDraftFoodCategoryRepository
                .findFoodCategoryIdsByCourseDraftId(COURSE_DRAFT_ID))
                .willReturn(List.of(1L));

        NearBasePlaceCandidateData candidateData =
                new NearBasePlaceCandidateData(
                        List.of(firstPlace, secondPlace),
                        java.util.Map.of(),
                        java.util.Map.of()
                );

        given(candidateLoader.load(
                anyList(),
                eq("CAFE"),
                anySet()
        )).willReturn(candidateData);

        EvaluatedNearBasePlace first =
                new EvaluatedNearBasePlace(
                        firstPlace,
                        100,
                        1,
                        Set.of(1L),
                        1,
                        11,
                        true
                );

        EvaluatedNearBasePlace second =
                new EvaluatedNearBasePlace(
                        secondPlace,
                        200,
                        0,
                        Set.of(),
                        0,
                        5,
                        true
                );

        NearBasePlaceRecommendationSelection selection =
                new NearBasePlaceRecommendationSelection(
                        List.of(first, second),
                        4
                );

        given(rankingPolicy.evaluateAndSelect(
                eq(candidateData),
                eq(basePlace),
                anySet(),
                anySet(),
                any(),
                eq("CAFE"),
                anyInt()
        )).willReturn(selection);

        RecommendationLog savedLog =
                mock(RecommendationLog.class);

        given(recommendationLogRepository.save(any(RecommendationLog.class)))
                .willReturn(savedLog);

        given(reasonResolver.resolve(any()))
                .willReturn(List.of("추천 이유"));

        CategoryRecommendedPlaceResponse response =
                mock(CategoryRecommendedPlaceResponse.class);

        given(responseAssembler.assemble(
                savedLog,
                basePlace,
                selection
        )).willReturn(response);

        CategoryRecommendedPlaceResponse result =
                service.getRecommendedPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        "CAFE",
                        10
                );

        assertThat(result).isSameAs(response);

        org.mockito.ArgumentCaptor<List<RecommendationResult>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);

        verify(recommendationResultRepository)
                .saveAll(captor.capture());

        List<RecommendationResult> savedResults =
                captor.getValue();

        assertThat(savedResults).hasSize(2);

        assertThat(savedResults)
                .extracting(RecommendationResult::getRankNo)
                .containsExactly(1, 2);

        assertThat(savedResults)
                .extracting(resultItem ->
                        resultItem.getPlace().getId()
                )
                .containsExactly(2L, 3L);
    }
}

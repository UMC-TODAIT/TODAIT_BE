package com.example.TODAIT__BE.domain.recommendation.service;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HotPlaceRecommendationResponse;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import com.example.TODAIT__BE.domain.recommendation.enums.RecommendationType;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationResultRepository;
import com.example.TODAIT__BE.domain.recommendation.service.support.EvaluatedHotPlace;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceCandidateData;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceCandidateLoader;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceRankingPolicy;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceRecommendationReasonResolver;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceRecommendationResponseAssembler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HotPlaceRecommendationServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long COURSE_DRAFT_ID = 10L;

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    @Mock
    private CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    @Mock
    private HotPlaceCandidateLoader candidateLoader;
    @Mock
    private HotPlaceRankingPolicy rankingPolicy;
    @Mock
    private HotPlaceRecommendationReasonResolver reasonResolver;
    @Mock
    private RecommendationLogRepository recommendationLogRepository;
    @Mock
    private RecommendationResultRepository recommendationResultRepository;
    @Mock
    private HotPlaceRecommendationResponseAssembler responseAssembler;

    private HotPlaceRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new HotPlaceRecommendationService(
                courseDraftRepository,
                courseDraftMoodTagRepository,
                courseDraftFoodCategoryRepository,
                candidateLoader,
                rankingPolicy,
                reasonResolver,
                recommendationLogRepository,
                new ObjectMapper(),
                recommendationResultRepository,
                responseAssembler
        );

        given(recommendationLogRepository.save(any(RecommendationLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void savesRecommendationLogAndRankedResultsThenAssemblesResponse() {
        CourseDraft courseDraft = courseDraft(
                MEMBER_ID,
                CourseDraftStatus.BASE_PLACE_SELECTING
        );
        Place place = mock(Place.class);
        given(place.getId()).willReturn(100L);

        HotPlaceCandidateData candidateData =
                new HotPlaceCandidateData(
                        List.of(place),
                        Map.of(),
                        Map.of()
                );

        EvaluatedHotPlace evaluated =
                new EvaluatedHotPlace(
                        place,
                        120,
                        true,
                        2,
                        1
                );

        HotPlaceRecommendationResponse.Result expectedResponse =
                new HotPlaceRecommendationResponse.Result(
                        null,
                        COURSE_DRAFT_ID,
                        CourseDraftStatus.BASE_PLACE_SELECTING,
                        true,
                        List.of()
                );

        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.of(courseDraft));
        given(courseDraftMoodTagRepository
                .findMoodTagIdsByCourseDraftId(COURSE_DRAFT_ID))
                .willReturn(List.of(10L, 11L));
        given(courseDraftFoodCategoryRepository
                .findFoodCategoryIdsByCourseDraftId(COURSE_DRAFT_ID))
                .willReturn(List.of(20L));
        given(candidateLoader.load()).willReturn(candidateData);
        given(rankingPolicy.evaluateAndSort(
                eq(candidateData),
                eq(java.util.Set.of(10L, 11L)),
                eq(java.util.Set.of(20L)),
                eq(37.5610),
                eq(126.9230),
                eq(true)
        )).willReturn(List.of(evaluated));
        given(reasonResolver.resolve(evaluated, true))
                .willReturn("현재 위치와 가까워요.");
        given(responseAssembler.assemble(
                any(RecommendationLog.class),
                eq(courseDraft),
                eq(true),
                anyList()
        )).willReturn(expectedResponse);

        HotPlaceRecommendationResponse.Result response =
                service.getHotPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        37.5610,
                        126.9230,
                        1
                );

        assertThat(response).isSameAs(expectedResponse);

        ArgumentCaptor<RecommendationLog> logCaptor =
                ArgumentCaptor.forClass(RecommendationLog.class);
        verify(recommendationLogRepository).save(logCaptor.capture());

        RecommendationLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getRecommendationType())
                .isEqualTo(RecommendationType.HOT_PLACE);
        assertThat(savedLog.getCourseDraft()).isSameAs(courseDraft);
        assertThat(savedLog.getUserLatitude()).isEqualTo(37.5610);
        assertThat(savedLog.getUserLongitude()).isEqualTo(126.9230);
        assertThat(savedLog.getRequestContext())
                .contains("\"locationAvailable\":true")
                .contains("\"limit\":1")
                .contains("\"policyVersion\":\"HOT_PLACE_V1\"");

        ArgumentCaptor<List<RecommendationResult>> resultCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(recommendationResultRepository)
                .saveAll(resultCaptor.capture());

        List<RecommendationResult> savedResults =
                resultCaptor.getValue();
        assertThat(savedResults).hasSize(1);
        assertThat(savedResults.get(0).getPlace()).isSameAs(place);
        assertThat(savedResults.get(0).getRankNo()).isEqualTo(1);
        assertThat(savedResults.get(0).getDistanceMeters()).isEqualTo(120);
        assertThat(savedResults.get(0).getMatchedMoodCount()).isEqualTo(2);
        assertThat(savedResults.get(0).getMatchedFoodCount()).isEqualTo(1);
        assertThat(savedResults.get(0).getReasonText())
                .isEqualTo("현재 위치와 가까워요.");
    }

    @Test
    void savesLogButDoesNotSaveResultsWhenNoCandidateExists() {
        CourseDraft courseDraft = courseDraft(
                MEMBER_ID,
                CourseDraftStatus.BASE_PLACE_SELECTING
        );
        HotPlaceCandidateData candidateData =
                new HotPlaceCandidateData(
                        List.of(),
                        Map.of(),
                        Map.of()
                );
        HotPlaceRecommendationResponse.Result expectedResponse =
                new HotPlaceRecommendationResponse.Result(
                        null,
                        COURSE_DRAFT_ID,
                        CourseDraftStatus.BASE_PLACE_SELECTING,
                        false,
                        List.of()
                );

        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.of(courseDraft));
        given(courseDraftMoodTagRepository
                .findMoodTagIdsByCourseDraftId(COURSE_DRAFT_ID))
                .willReturn(List.of(10L, 11L));
        given(courseDraftFoodCategoryRepository
                .findFoodCategoryIdsByCourseDraftId(COURSE_DRAFT_ID))
                .willReturn(List.of(20L));
        given(candidateLoader.load()).willReturn(candidateData);
        given(rankingPolicy.evaluateAndSort(
                any(),
                any(),
                any(),
                eq(null),
                eq(null),
                eq(false)
        )).willReturn(List.of());
        given(responseAssembler.assemble(
                any(RecommendationLog.class),
                eq(courseDraft),
                eq(false),
                eq(List.of())
        )).willReturn(expectedResponse);

        HotPlaceRecommendationResponse.Result response =
                service.getHotPlaces(
                        MEMBER_ID,
                        COURSE_DRAFT_ID,
                        null,
                        null,
                        null
                );

        assertThat(response).isSameAs(expectedResponse);
        verify(recommendationLogRepository)
                .save(any(RecommendationLog.class));
        verify(recommendationResultRepository, never())
                .saveAll(anyList());
    }

    @Test
    void rejectsSizeOutsideAllowedRange() {
        assertThatThrownBy(() -> service.getHotPlaces(
                MEMBER_ID,
                COURSE_DRAFT_ID,
                null,
                null,
                11
        ))
                .isInstanceOf(RecommendationException.class)
                .extracting(exception ->
                        ((RecommendationException) exception)
                                .getErrorCode()
                )
                .isEqualTo(
                        RecommendationErrorCode.INVALID_HOT_PLACE_SIZE
                );

        verify(courseDraftRepository, never()).findById(any());
    }

    @Test
    void rejectsWhenOnlyOneCoordinateIsProvided() {
        assertThatThrownBy(() -> service.getHotPlaces(
                MEMBER_ID,
                COURSE_DRAFT_ID,
                37.5610,
                null,
                4
        ))
                .isInstanceOf(RecommendationException.class)
                .extracting(exception ->
                        ((RecommendationException) exception)
                                .getErrorCode()
                )
                .isEqualTo(
                        RecommendationErrorCode.INCOMPLETE_COORDINATES
                );

        verify(courseDraftRepository, never()).findById(any());
    }

    @Test
    void rejectsInvalidCoordinates() {
        assertThatThrownBy(() -> service.getHotPlaces(
                MEMBER_ID,
                COURSE_DRAFT_ID,
                91.0,
                126.9230,
                4
        ))
                .isInstanceOf(RecommendationException.class)
                .extracting(exception ->
                        ((RecommendationException) exception)
                                .getErrorCode()
                )
                .isEqualTo(
                        RecommendationErrorCode.INVALID_COORDINATES
                );

        verify(courseDraftRepository, never()).findById(any());
    }

    @Test
    void rejectsCourseDraftOwnedByAnotherMember() {
        CourseDraft courseDraft = courseDraft(
                999L,
                CourseDraftStatus.BASE_PLACE_SELECTING
        );
        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.of(courseDraft));

        assertThatThrownBy(() -> service.getHotPlaces(
                MEMBER_ID,
                COURSE_DRAFT_ID,
                null,
                null,
                4
        ))
                .isInstanceOf(CourseException.class)
                .extracting(exception ->
                        ((CourseException) exception).getErrorCode()
                )
                .isEqualTo(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED);

        verify(candidateLoader, never()).load();
    }

    @Test
    void rejectsCourseDraftInUnsupportedStatus() {
        CourseDraft courseDraft = courseDraft(
                MEMBER_ID,
                CourseDraftStatus.FOOD_SELECTING
        );
        given(courseDraftRepository.findById(COURSE_DRAFT_ID))
                .willReturn(Optional.of(courseDraft));

        assertThatThrownBy(() -> service.getHotPlaces(
                MEMBER_ID,
                COURSE_DRAFT_ID,
                null,
                null,
                4
        ))
                .isInstanceOf(CourseException.class)
                .extracting(exception ->
                        ((CourseException) exception).getErrorCode()
                )
                .isEqualTo(CourseErrorCode.INVALID_COURSE_DRAFT_STATUS);

        verify(candidateLoader, never()).load();
    }

    private CourseDraft courseDraft(
            Long ownerMemberId,
            CourseDraftStatus status
    ) {
        CourseDraft courseDraft = mock(CourseDraft.class);
        Member member = mock(Member.class);

        given(courseDraft.getId()).willReturn(COURSE_DRAFT_ID);
        given(courseDraft.getMember()).willReturn(member);
        given(courseDraft.getStatus()).willReturn(status);
        given(member.getId()).willReturn(ownerMemberId);

        return courseDraft;
    }
}

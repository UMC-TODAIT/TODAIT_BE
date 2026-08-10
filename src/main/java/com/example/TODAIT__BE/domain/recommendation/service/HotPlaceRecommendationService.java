package com.example.TODAIT__BE.domain.recommendation.service;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.recommendation.code.HotPlaceRecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationLogErrorCode;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HotPlaceRecommendationResponse;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import com.example.TODAIT__BE.domain.recommendation.enums.RecommendationType;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationResultRepository;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceCandidateLoader;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceCandidateLoader.CandidateData;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceRankingPolicy;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceRankingPolicy.EvaluatedPlace;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceRecommendationResponseAssembler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HotPlaceRecommendationService {

    private static final int DEFAULT_SIZE = 4;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 10;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    private final CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    private final HotPlaceCandidateLoader candidateLoader;
    private final HotPlaceRankingPolicy rankingPolicy;
    private final RecommendationLogRepository recommendationLogRepository;
    private final ObjectMapper objectMapper;
    private final RecommendationResultRepository recommendationResultRepository;
    private final HotPlaceRecommendationResponseAssembler responseAssembler;

    @Transactional
    public HotPlaceRecommendationResponse.Result getHotPlaces(
      Long memberId,
      Long courseDraftId,
      Double latitude,
      Double longitude,
      Integer size
    ){
        int resolvedSize = validateAndResolveSize(size);

        boolean locationAvailable =
                validateAndResolveLocation(
                        latitude,
                        longitude
                );

        CourseDraft courseDraft =
                getAndValidateCourseDraft(
                        courseDraftId,
                        memberId
                );

        Set<Long> selectedMoodTagIds =
                getSelectedMoodTagIds(courseDraft.getId());

        Set<Long> selectedFoodCategoryIds =
                getSelectedFoodCategoryIds(courseDraft.getId());

        CandidateData candidateData =
                candidateLoader.load();

        List<EvaluatedPlace> evaluatedHotPlaces =
                rankingPolicy.evaluateAndSort(
                        candidateData,
                        selectedMoodTagIds,
                        selectedFoodCategoryIds,
                        latitude,
                        longitude,
                        locationAvailable
                );

        List<EvaluatedPlace> selectedPlaces =
                evaluatedHotPlaces.stream()
                        .limit(resolvedSize)
                        .toList();

        RecommendationLog recommendationLog =
                saveRecommendationLog(
                        courseDraft,
                        latitude,
                        longitude,
                        locationAvailable,
                        selectedMoodTagIds,
                        selectedFoodCategoryIds,
                        resolvedSize
                );
        List<RecommendationResult> recommendationResults =
                saveRecommendationResults(
                        recommendationLog,
                        selectedPlaces,
                        locationAvailable
                );
        return responseAssembler.assemble(
                recommendationLog,
                courseDraft,
                locationAvailable,
                recommendationResults
        );
    }

    private int validateAndResolveSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new RecommendationException(
                    HotPlaceRecommendationErrorCode.INVALID_HOT_PLACE_SIZE
            );
        }

        return size;
    }

    private boolean validateAndResolveLocation(
            Double latitude,
            Double longitude
    ) {
        boolean latitudeMissing = latitude == null;
        boolean longitudeMissing = longitude == null;

        if (latitudeMissing != longitudeMissing) {
            throw new RecommendationException(
                    HotPlaceRecommendationErrorCode.INCOMPLETE_COORDINATES
            );
        }

        if (latitudeMissing) {
            return false;
        }

        boolean invalidNumber =
                !Double.isFinite(latitude)
                        || !Double.isFinite(longitude);

        boolean invalidLatitude =
                latitude < -90 || latitude > 90;

        boolean invalidLongitude =
                longitude < -180 || longitude > 180;

        if (invalidNumber || invalidLatitude || invalidLongitude) {
            throw new RecommendationException(
                    HotPlaceRecommendationErrorCode.INVALID_COORDINATES
            );
        }

        return true;
    }

    private CourseDraft getAndValidateCourseDraft(
            Long courseDraftId,
            Long memberId
    ) {
        CourseDraft courseDraft =
                courseDraftRepository.findById(courseDraftId)
                        .orElseThrow(() ->
                                new CourseException(
                                        CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND
                                )
                        );

        if (!courseDraft.getMember()
                .getId()
                .equals(memberId)) {
            throw new CourseException(
                    CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED
            );
        }

        if (courseDraft.getStatus()
                != CourseDraftStatus.BASE_PLACE_SELECTING) {
            throw new CourseException(
                    CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT
            );
        }

        return courseDraft;
    }

    private Set<Long> getSelectedMoodTagIds(
            Long courseDraftId
    ) {
        return new HashSet<>(
                courseDraftMoodTagRepository
                        .findMoodTagIdsByCourseDraftId(
                                courseDraftId
                        )
        );
    }

    private Set<Long> getSelectedFoodCategoryIds(
            Long courseDraftId
    ) {
        return new HashSet<>(
                courseDraftFoodCategoryRepository
                        .findFoodCategoryIdsByCourseDraftId(
                                courseDraftId
                        )
        );
    }

    private RecommendationLog saveRecommendationLog(
            CourseDraft courseDraft,
            Double latitude,
            Double longitude,
            boolean locationAvailable,
            Set<Long> selectedMoodTagIds,
            Set<Long> selectedFoodCategoryIds,
            int resolvedSize
    ){
        HotPlaceRequestContext requestContext =
                new HotPlaceRequestContext(
                        locationAvailable,
                        500,
                        selectedMoodTagIds,
                        selectedFoodCategoryIds,
                        resolvedSize,
                        "HOT_PLACE_V1"
                );

        RecommendationLog recommendationLog =
                RecommendationLog.builder()
                        .member(courseDraft.getMember())
                        .recommendationType(
                                RecommendationType.HOT_PLACE
                        )
                        .courseDraft(courseDraft)
                        .basePlace(null)
                        .area(null)
                        .placeCategory(null)
                        .userLatitude(
                                locationAvailable ? latitude : null
                        )
                        .userLongitude(
                                locationAvailable ? longitude : null
                        )
                        .requestContext(
                                serializeRequestContext(requestContext)
                        )
                        .build();

        return recommendationLogRepository.save(
                recommendationLog
        );

    }

    private String serializeRequestContext(
            HotPlaceRequestContext requestContext
    ) {
        try {
            return objectMapper.writeValueAsString(
                    requestContext
            );
        } catch (JsonProcessingException exception) {
            throw new RecommendationException(
                    RecommendationLogErrorCode
                            .REQUEST_CONTEXT_SERIALIZATION_FAILED,
                    exception
            );
        }
    }

    private List<RecommendationResult> saveRecommendationResults(
            RecommendationLog recommendationLog,
            List<EvaluatedPlace> selectedPlaces,
            boolean locationAvailable
    ) {
        if (selectedPlaces.isEmpty()) {
            return List.of();
        }

        List<RecommendationResult> results =
                new java.util.ArrayList<>(
                        selectedPlaces.size()
                );

        for (int index = 0;
             index < selectedPlaces.size();
             index++) {

            EvaluatedPlace evaluated =
                    selectedPlaces.get(index);

            int rank = index + 1;

            String recommendationReason =
                    resolveRecommendationReason(
                            evaluated,
                            locationAvailable
                    );

            RecommendationResult result =
                    RecommendationResult.forPlace(
                            recommendationLog,
                            evaluated.place(),
                            rank,
                            recommendationReason,
                            evaluated.distanceMeters(),
                            evaluated.matchedMoodCount(),
                            evaluated.matchedFoodCount(),
                            null
                    );

            results.add(result);
        }

        recommendationResultRepository.saveAll(results);

        return results;
    }

    private String resolveRecommendationReason(
            EvaluatedPlace evaluated,
            boolean locationAvailable
    ) {
        if (locationAvailable
                && Boolean.TRUE.equals(evaluated.nearby())) {
            return "현재 위치와 가까워요.";
        }

        if (evaluated.matchedMoodCount() > 0) {
            return "선택한 분위기와 잘 어울려요.";
        }

        if (evaluated.matchedFoodCount() != null
                && evaluated.matchedFoodCount() > 0) {
            return "원하는 음식 취향과 잘 맞아요.";
        }

        if (evaluated.place().getArea() != null
                && evaluated.place().getArea().getName() != null
                && !evaluated.place().getArea().getName().isBlank()) {
            return evaluated.place().getArea().getName()
                    + " 추천 장소예요.";
        }

        if (evaluated.place().getDefaultRecommendReason() != null
                && !evaluated.place().getDefaultRecommendReason().isBlank()) {
            return evaluated.place().getDefaultRecommendReason();
        }

        return "지금 가기 좋은 추천 장소예요.";
    }

    private record HotPlaceRequestContext(
            boolean locationAvailable,
            int nearbyDistanceMeters,
            Set<Long> selectedMoodTagIds,
            Set<Long> selectedFoodCategoryIds,
            int limit,
            String policyVersion
    ) {
        private HotPlaceRequestContext {
            selectedMoodTagIds = Set.copyOf(selectedMoodTagIds);
            selectedFoodCategoryIds =
                    Set.copyOf(selectedFoodCategoryIds);
        }
    }
}

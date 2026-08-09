package com.example.TODAIT__BE.domain.recommendation.service;

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
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendedPlaceErrorCode;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationLogErrorCode;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import com.example.TODAIT__BE.domain.recommendation.enums.RecommendationType;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationResultRepository;
import com.example.TODAIT__BE.domain.recommendation.service.support.EvaluatedNearBasePlace;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceCandidateLoader;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceCandidateLoader.CandidateData;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceRankingPolicy;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceRecommendationSelection;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomyErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.FoodCategoryRepository;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.TODAIT__BE.domain.recommendation.dto.response.CategoryRecommendedPlaceResponse;
import com.example.TODAIT__BE.domain.recommendation.service.support.NearBasePlaceResponseAssembler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryRecommendedPlaceService {

    private static final int DEFAULT_SIZE = 10;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 20;

    private static final String DESSERT_CODE = "DESSERT";

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;
    private final CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    private final CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;

    private final PlaceCategoryRepository placeCategoryRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    private final NearBasePlaceCandidateLoader candidateLoader;
    private final NearBasePlaceRankingPolicy rankingPolicy;

    private final RecommendationLogRepository recommendationLogRepository;
    private final RecommendationResultRepository recommendationResultRepository;

    private final ObjectMapper objectMapper;
    private final NearBasePlaceResponseAssembler responseAssembler;

    private static final Set<String> SUPPORTED_PLACE_CATEGORY_CODES =
            Set.of(
                    "CAFE",
                    "ACTIVITY",
                    "RESTAURANT",
                    "BAR"
            );

    @Transactional
    public CategoryRecommendedPlaceResponse getRecommendedPlaces(
            Long memberId,
            Long courseDraftId,
            String placeCategoryCode,
            Integer size
    ) {
        int resolvedSize = validateAndResolveSize(size);

        CourseDraft courseDraft =
                getAndValidateCourseDraft(courseDraftId, memberId);

        List<CourseDraftPlace> draftPlaces =
                courseDraftPlaceRepository
                        .findByCourseDraftWithPlaceOrderByVisitOrderAsc(
                                courseDraft
                        );

        Place basePlace = resolveBasePlace(draftPlaces);

        PlaceCategory placeCategory =
                getPlaceCategory(placeCategoryCode);

        Set<Long> selectedMoodTagIds =
                new HashSet<>(
                        courseDraftMoodTagRepository
                                .findMoodTagIdsByCourseDraftId(
                                        courseDraftId
                                )
                );

        Set<Long> selectedFoodCategoryIds =
                new HashSet<>(
                        courseDraftFoodCategoryRepository
                                .findFoodCategoryIdsByCourseDraftId(
                                        courseDraftId
                                )
                );

        Set<Long> excludedPlaceIds =
                draftPlaces.stream()
                        .map(CourseDraftPlace::getPlace)
                        .map(Place::getId)
                        .collect(java.util.stream.Collectors.toSet());

        List<String> candidateAreaCodes =
                resolveCandidateAreaCodes(
                        basePlace.getArea().getCode()
                );

        CandidateData candidateData =
                candidateLoader.load(
                        candidateAreaCodes,
                        placeCategory.getCode(),
                        excludedPlaceIds
                );

        Long dessertFoodCategoryId =
                resolveDessertFoodCategoryId();

        NearBasePlaceRecommendationSelection selection =
                rankingPolicy.evaluateAndSelect(
                        candidateData,
                        basePlace,
                        selectedMoodTagIds,
                        selectedFoodCategoryIds,
                        dessertFoodCategoryId,
                        placeCategory.getCode(),
                        resolvedSize
                );

        RecommendationLog recommendationLog =
                saveRecommendationLog(
                        courseDraft,
                        basePlace,
                        placeCategory,
                        selectedMoodTagIds,
                        selectedFoodCategoryIds,
                        excludedPlaceIds,
                        candidateAreaCodes,
                        selection.appliedRelaxationLevel(),
                        resolvedSize
                );

        saveRecommendationResults(
                recommendationLog,
                selection.places()
        );

        return responseAssembler.assemble(
                recommendationLog,
                basePlace,
                selection
        );
    }

    private int validateAndResolveSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }

        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new RecommendationException(
                    RecommendedPlaceErrorCode.INVALID_PLACE_SIZE
            );
        }

        return size;
    }

    private CourseDraft getAndValidateCourseDraft(
            Long courseDraftId,
            Long memberId
    ) {
        CourseDraft courseDraft =
                courseDraftRepository.findById(courseDraftId)
                        .orElseThrow(() ->
                                new CourseException(
                                        CourseErrorCode.COURSE_DRAFT_NOT_FOUND
                                )
                        );

        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(
                    CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED
            );
        }

        if (courseDraft.getStatus()
                != CourseDraftStatus.PLACE_SELECTING) {
            throw new CourseException(
                    CourseErrorCode.COURSE_DRAFT_STATUS_CONFLICT
            );
        }

        return courseDraft;
    }

    private Place resolveBasePlace(
            List<CourseDraftPlace> draftPlaces
    ) {
        List<CourseDraftPlace> basePlaces =
                draftPlaces.stream()
                        .filter(place ->
                                place.getPlaceRole()
                                        == PlaceRole.BASE
                        )
                        .toList();

        if (basePlaces.size() != 1) {
            throw new CourseException(
                    CourseErrorCode.COURSE_DRAFT_BASE_PLACE_CONFLICT
            );
        }

        CourseDraftPlace baseDraftPlace = basePlaces.get(0);

        if (!Integer.valueOf(1).equals(
                baseDraftPlace.getVisitOrder()
        )) {
            throw new CourseException(
                    CourseErrorCode.COURSE_DRAFT_BASE_PLACE_CONFLICT
            );
        }

        return baseDraftPlace.getPlace();
    }

    private PlaceCategory getPlaceCategory(
            String placeCategoryCode
    ) {
        if (placeCategoryCode == null
                || placeCategoryCode.isBlank()
                || !SUPPORTED_PLACE_CATEGORY_CODES.contains(
                placeCategoryCode
        )) {

            throw new TaxonomyException(
                    TaxonomyErrorCode.PLACE_CATEGORY_NOT_SUPPORTED
            );
        }

        return placeCategoryRepository
                .findByCode(placeCategoryCode)
                .filter(PlaceCategory::getIsActive)
                .orElseThrow(() ->
                        new TaxonomyException(
                                TaxonomyErrorCode.PLACE_CATEGORY_NOT_SUPPORTED
                        )
                );
    }

    private Long resolveDessertFoodCategoryId() {
        return foodCategoryRepository
                .findByCode(DESSERT_CODE)
                .filter(FoodCategory::getIsActive)
                .map(FoodCategory::getId)
                .orElse(null);
    }

    private List<String> resolveCandidateAreaCodes(
            String baseAreaCode
    ) {
        return switch (baseAreaCode) {
            case "HONGDAE" ->
                    List.of("HONGDAE", "YEONNAM");

            case "YEONNAM" ->
                    List.of("YEONNAM", "HONGDAE");

            case "SEONGSU" ->
                    List.of("SEONGSU");

            default ->
                    throw new TaxonomyException(
                            TaxonomyErrorCode.AREA_NOT_SUPPORTED
                    );
        };
    }

    private RecommendationLog saveRecommendationLog(
            CourseDraft courseDraft,
            Place basePlace,
            PlaceCategory placeCategory,
            Set<Long> selectedMoodTagIds,
            Set<Long> selectedFoodCategoryIds,
            Set<Long> excludedPlaceIds,
            List<String> candidateAreaCodes,
            int appliedRelaxationLevel,
            int limit
    ) {
        Map<String, Object> requestContext =
                new LinkedHashMap<>();

        requestContext.put(
                "sourcePolicy",
                "PM_PLACE_RECOMMENDATION_DOCUMENT"
        );
        requestContext.put(
                "screenPhase",
                "AFTER_BASE_PLACE_CONFIRMATION"
        );
        requestContext.put(
                "selectedCategoryCode",
                placeCategory.getCode()
        );
        requestContext.put(
                "selectedMoodTagIds",
                selectedMoodTagIds
        );
        requestContext.put(
                "selectedFoodCategoryIds",
                selectedFoodCategoryIds
        );
        requestContext.put(
                "excludedPlaceIds",
                excludedPlaceIds
        );
        requestContext.put(
                "baseAreaCode",
                basePlace.getArea().getCode()
        );
        requestContext.put(
                "candidateAreaCodes",
                candidateAreaCodes
        );
        requestContext.put(
                "appliedRelaxationLevel",
                appliedRelaxationLevel
        );
        requestContext.put(
                "limit",
                limit
        );
        requestContext.put(
                "policyVersion",
                "NEAR_BASE_V1"
        );

        RecommendationLog recommendationLog =
                RecommendationLog.builder()
                        .member(courseDraft.getMember())
                        .recommendationType(
                                RecommendationType.NEAR_BASE_PLACE
                        )
                        .courseDraft(courseDraft)
                        .basePlace(basePlace)
                        .area(basePlace.getArea())
                        .placeCategory(placeCategory)
                        .userLatitude(null)
                        .userLongitude(null)
                        .requestContext(
                                serializeRequestContext(
                                        requestContext
                                )
                        )
                        .build();

        return recommendationLogRepository.save(
                recommendationLog
        );
    }

    private String serializeRequestContext(
            Object requestContext
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

    private void saveRecommendationResults(
            RecommendationLog recommendationLog,
            List<EvaluatedNearBasePlace> selectedPlaces
    ) {
        if (selectedPlaces.isEmpty()) {
            return;
        }

        List<RecommendationResult> results =
                new ArrayList<>(selectedPlaces.size());

        for (int index = 0;
             index < selectedPlaces.size();
             index++) {

            EvaluatedNearBasePlace evaluated =
                    selectedPlaces.get(index);

            List<String> recommendationReasons =
                    evaluated.recommendationReasons();

            String reasonText =
                    recommendationReasons.isEmpty()
                            ? null
                            : String.join(
                                    " ",
                                    recommendationReasons
                            );

            RecommendationResult result =
                    RecommendationResult.forPlace(
                            recommendationLog,
                            evaluated.place(),
                            index + 1,
                            reasonText,
                            evaluated.distanceMeters(),
                            evaluated.matchedMoodCount(),
                            evaluated.matchedFoodCount(),
                            BigDecimal.valueOf(
                                    evaluated.internalScore()
                            )
                    );

            results.add(result);
        }

        recommendationResultRepository.saveAll(results);
    }
}

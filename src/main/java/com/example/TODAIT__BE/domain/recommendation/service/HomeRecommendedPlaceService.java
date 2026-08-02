package com.example.TODAIT__BE.domain.recommendation.service;

import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedPlaceListResponse;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedPlaceResponse;
import com.example.TODAIT__BE.domain.recommendation.dto.response.RecommendedPlaceAreaResponse;
import com.example.TODAIT__BE.domain.recommendation.dto.response.RecommendedPlaceCategoryResponse;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import com.example.TODAIT__BE.domain.recommendation.enums.RecommendationType;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeRecommendedPlaceService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 2;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 20;

    private static final int NEARBY_THRESHOLD_METERS = 500;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    /*
     * 위치정보가 없을 때 적용할 지역 라운드 로빈 순서
     */
    private static final List<String> AREA_CODES =
            List.of("HONGDAE", "SEONGSU", "YEONNAM");

    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;
    private final RecommendationLogRepository recommendationLogRepository;
    private final RecommendationResultRepository recommendationResultRepository;
    private final ObjectMapper objectMapper;

    public HomeRecommendedPlaceService(
            PlaceRepository placeRepository,
            MemberRepository memberRepository,
            RecommendationLogRepository recommendationLogRepository,
            RecommendationResultRepository recommendationResultRepository,
            ObjectMapper objectMapper
    ) {
        this.placeRepository = placeRepository;
        this.memberRepository = memberRepository;
        this.recommendationLogRepository = recommendationLogRepository;
        this.recommendationResultRepository = recommendationResultRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public HomeRecommendedPlaceListResponse getHomeRecommendedPlaces(
            Long memberId,
            Integer pageParam,
            Integer sizeParam,
            Double latitude,
            Double longitude
    ) {
        int page = resolvePage(pageParam);
        int size = resolveSize(sizeParam);

        validateLocation(latitude, longitude);

        boolean locationAvailable =
                latitude != null && longitude != null;

        /*
         * 1. 추천 가능한 운영자 관리 장소 후보 조회
         */
        List<Place> candidates =
                placeRepository.findHomeRecommendedPlaceCandidates(
                        PlaceReviewStatus.APPROVED,
                        PlaceExposureStatus.ACTIVE,
                        AREA_CODES
                );

        /*
         * 2. placeId 기준 중복 제거
         */
        List<Place> distinctCandidates =
                removeDuplicatesByPlaceId(candidates);

        /*
         * 3. 위치정보 유무에 따른 전체 추천 순서 확정
         */
        List<RankedPlace> fullOrder = locationAvailable
                ? buildLocationBasedOrder(
                distinctCandidates,
                latitude,
                longitude
        )
                : buildAreaBalancedOrder(distinctCandidates);

        /*
         * 4. 전체 추천 순서 확정 후 page와 size 적용
         */
        long from = (long) page * size;
        List<RankedPlace> pagePlaces =
                slice(fullOrder, from, size);

        /*
         * 5. 추천 요청 로그 1건 저장
         */
        RecommendationLog recommendationLog =
                saveRecommendationLog(
                        memberId,
                        page,
                        size,
                        latitude,
                        longitude,
                        locationAvailable
                );

        /*
         * 6. 응답 DTO 및 추천 결과 저장
         */
        List<HomeRecommendedPlaceResponse> placeResponses =
                buildResponsesAndSaveResults(
                        pagePlaces,
                        recommendationLog,
                        locationAvailable
                );

        return new HomeRecommendedPlaceListResponse(
                recommendationLog.getId(),
                page,
                size,
                locationAvailable,
                placeResponses
        );
    }

    private int resolvePage(Integer pageParam) {
        if (pageParam == null) {
            return DEFAULT_PAGE;
        }

        if (pageParam < 0) {
            throw new RecommendationException(
                    RecommendationErrorCode.INVALID_PAGE
            );
        }

        return pageParam;
    }

    private int resolveSize(Integer sizeParam) {
        if (sizeParam == null) {
            return DEFAULT_SIZE;
        }

        if (sizeParam < MIN_SIZE || sizeParam > MAX_SIZE) {
            throw new RecommendationException(
                    RecommendationErrorCode.INVALID_PLACE_SIZE
            );
        }

        return sizeParam;
    }

    private void validateLocation(
            Double latitude,
            Double longitude
    ) {
        boolean onlyLatitude =
                latitude != null && longitude == null;

        boolean onlyLongitude =
                latitude == null && longitude != null;

        if (onlyLatitude || onlyLongitude) {
            throw new RecommendationException(
                    RecommendationErrorCode.INVALID_LOCATION_PAIR
            );
        }

        if (latitude == null) {
            return;
        }

        boolean invalidFiniteValue =
                !Double.isFinite(latitude)
                        || !Double.isFinite(longitude);

        boolean invalidLatitude =
                latitude < -90.0 || latitude > 90.0;

        boolean invalidLongitude =
                longitude < -180.0 || longitude > 180.0;

        if (invalidFiniteValue
                || invalidLatitude
                || invalidLongitude) {
            throw new RecommendationException(
                    RecommendationErrorCode.INVALID_LOCATION_RANGE
            );
        }
    }

    private List<Place> removeDuplicatesByPlaceId(
            List<Place> candidates
    ) {
        Map<Long, Place> placeById = new LinkedHashMap<>();

        for (Place place : candidates) {
            placeById.putIfAbsent(place.getId(), place);
        }

        return new ArrayList<>(placeById.values());
    }

    /**
     * 위치정보가 있는 경우:
     * isNearby DESC
     * → distanceMeters ASC
     * → operatorPriority ASC
     * → createdAt ASC
     * → placeId ASC
     */
    private List<RankedPlace> buildLocationBasedOrder(
            List<Place> candidates,
            double userLatitude,
            double userLongitude
    ) {
        List<RankedPlace> rankedPlaces =
                new ArrayList<>(candidates.size());

        for (Place place : candidates) {
            int distanceMeters = calculateDistanceMeters(
                    userLatitude,
                    userLongitude,
                    place.getLatitude(),
                    place.getLongitude()
            );

            boolean nearby =
                    distanceMeters <= NEARBY_THRESHOLD_METERS;

            rankedPlaces.add(new RankedPlace(
                    place,
                    distanceMeters,
                    nearby
            ));
        }

        Comparator<RankedPlace> comparator = Comparator
                /*
                 * true가 false보다 앞에 오도록 역순
                 */
                .comparing(
                        RankedPlace::nearby,
                        Comparator.reverseOrder()
                )
                .thenComparing(RankedPlace::distanceMeters)
                .thenComparing(
                        rankedPlace ->
                                rankedPlace.place().getOperatorPriority(),
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
                .thenComparing(
                        rankedPlace ->
                                rankedPlace.place().getCreatedAt(),
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
                .thenComparing(
                        rankedPlace ->
                                rankedPlace.place().getId()
                );

        rankedPlaces.sort(comparator);

        return rankedPlaces;
    }

    /**
     * 위치정보가 없는 경우:
     * 1. 지역 안에서 operatorPriority, createdAt, id 순으로 정렬
     * 2. 홍대 → 성수 → 연남 순서로 라운드 로빈 구성
     */
    private List<RankedPlace> buildAreaBalancedOrder(
            List<Place> candidates
    ) {
        Map<String, List<Place>> placesByArea =
                new LinkedHashMap<>();

        for (String areaCode : AREA_CODES) {
            placesByArea.put(areaCode, new ArrayList<>());
        }

        for (Place place : candidates) {
            if (place.getArea() == null) {
                continue;
            }

            List<Place> areaPlaces =
                    placesByArea.get(place.getArea().getCode());

            if (areaPlaces != null) {
                areaPlaces.add(place);
            }
        }

        Comparator<Place> areaInternalOrder = Comparator
                .comparing(
                        Place::getOperatorPriority,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
                .thenComparing(
                        Place::getCreatedAt,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
                .thenComparing(Place::getId);

        placesByArea.values()
                .forEach(areaPlaces ->
                        areaPlaces.sort(areaInternalOrder)
                );

        int maximumAreaSize = placesByArea.values()
                .stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        List<RankedPlace> fullOrder = new ArrayList<>();

        for (int round = 0; round < maximumAreaSize; round++) {
            for (String areaCode : AREA_CODES) {
                List<Place> areaPlaces =
                        placesByArea.get(areaCode);

                if (areaPlaces == null
                        || round >= areaPlaces.size()) {
                    continue;
                }

                fullOrder.add(new RankedPlace(
                        areaPlaces.get(round),
                        null,
                        null
                ));
            }
        }

        return fullOrder;
    }

    private List<RankedPlace> slice(
            List<RankedPlace> fullOrder,
            long from,
            int size
    ) {
        if (from >= fullOrder.size()) {
            return List.of();
        }

        int start = (int) from;
        int end = Math.min(start + size, fullOrder.size());

        return new ArrayList<>(
                fullOrder.subList(start, end)
        );
    }

    private RecommendationLog saveRecommendationLog(
            Long memberId,
            int page,
            int size,
            Double latitude,
            Double longitude,
            boolean locationAvailable
    ) {
        /*
         * 기존 홈 추천 코스 서비스와 동일하게
         * 실제 조회 쿼리 없이 FK 참조를 생성한다.
         */
        Member member =
                memberRepository.getReferenceById(memberId);

        RecommendationLog recommendationLog =
                RecommendationLog.builder()
                        .member(member)
                        .recommendationType(
                                RecommendationType.HOME_PLACE
                        )
                        .courseDraft(null)
                        .basePlace(null)
                        .area(null)
                        .placeCategory(null)
                        .userLatitude(latitude)
                        .userLongitude(longitude)
                        .requestContext(
                                buildRequestContext(
                                        page,
                                        size,
                                        locationAvailable
                                )
                        )
                        .build();

        return recommendationLogRepository.save(
                recommendationLog
        );
    }

    private List<HomeRecommendedPlaceResponse>
    buildResponsesAndSaveResults(
            List<RankedPlace> pagePlaces,
            RecommendationLog recommendationLog,
            boolean locationAvailable
    ) {
        if (pagePlaces.isEmpty()) {
            return List.of();
        }

        List<HomeRecommendedPlaceResponse> responses =
                new ArrayList<>(pagePlaces.size());

        List<RecommendationResult> results =
                new ArrayList<>(pagePlaces.size());

        int rank = 1;

        for (RankedPlace rankedPlace : pagePlaces) {
            Place place = rankedPlace.place();

            String recommendationReason =
                    buildRecommendationReason(
                            place,
                            rankedPlace,
                            locationAvailable
                    );

            responses.add(
                    toResponse(
                            place,
                            rankedPlace,
                            rank,
                            recommendationReason
                    )
            );

            results.add(
                    RecommendationResult.forPlace(
                            recommendationLog,
                            place,
                            rank,
                            recommendationReason,
                            rankedPlace.distanceMeters(),
                            null,
                            null,
                            null
                    )
            );

            rank++;
        }

        recommendationResultRepository.saveAll(results);

        return responses;
    }

    private HomeRecommendedPlaceResponse toResponse(
            Place place,
            RankedPlace rankedPlace,
            int rank,
            String recommendationReason
    ) {
        RecommendedPlaceAreaResponse areaResponse =
                new RecommendedPlaceAreaResponse(
                        place.getArea().getId(),
                        place.getArea().getCode(),
                        place.getArea().getName()
                );

        RecommendedPlaceCategoryResponse categoryResponse =
                new RecommendedPlaceCategoryResponse(
                        place.getPlaceCategory().getId(),
                        place.getPlaceCategory().getCode(),
                        place.getPlaceCategory().getName()
                );

        return new HomeRecommendedPlaceResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getRoadAddress(),
                place.getLatitude(),
                place.getLongitude(),
                areaResponse,
                categoryResponse,
                place.getSubCategory(),
                place.getDefaultImageUrl(),
                rank,
                rankedPlace.distanceMeters(),
                rankedPlace.nearby(),
                recommendationReason,
                true
        );
    }

    private String buildRecommendationReason(
            Place place,
            RankedPlace rankedPlace,
            boolean locationAvailable
    ) {
        if (locationAvailable
                && Boolean.TRUE.equals(rankedPlace.nearby())) {
            return "현재 위치와 가까워요.";
        }

        return place.getArea().getName()
                + " 추천 장소예요.";
    }

    private String buildRequestContext(
            int page,
            int size,
            boolean locationAvailable
    ) {
        Map<String, Object> context =
                new LinkedHashMap<>();

        context.put(
                "sourcePolicy",
                "RESEARCH_DOCUMENT"
        );
        context.put(
                "locationStatus",
                locationAvailable
                        ? "AVAILABLE"
                        : "UNAVAILABLE"
        );
        context.put(
                "candidatePolicy",
                "OPERATOR_MANAGED_PLACES"
        );
        context.put("includedAreaCodes", AREA_CODES);

        if (locationAvailable) {
            context.put(
                    "nearbyThresholdMeters",
                    NEARBY_THRESHOLD_METERS
            );
            context.put(
                    "sortPolicy",
                    List.of(
                            "IS_NEARBY_DESC",
                            "DISTANCE_ASC",
                            "OPERATOR_PRIORITY_ASC",
                            "CREATED_AT_ASC",
                            "PLACE_ID_ASC"
                    )
            );
        } else {
            context.put(
                    "fallbackPolicy",
                    "AREA_BALANCED_ROTATION"
            );
            context.put(
                    "sortPolicy",
                    List.of(
                            "AREA_BALANCED_ROTATION",
                            "OPERATOR_PRIORITY_ASC",
                            "CREATED_AT_ASC",
                            "PLACE_ID_ASC"
                    )
            );
        }

        context.put("page", page);
        context.put("limit", size);
        context.put(
                "policyVersion",
                "HOME_PLACE_V2"
        );

        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 두 위·경도 사이의 직선거리를 계산하는 Haversine 공식
     */
    private int calculateDistanceMeters(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        double latitudeDelta =
                Math.toRadians(latitude2 - latitude1);

        double longitudeDelta =
                Math.toRadians(longitude2 - longitude1);

        double latitude1Radians =
                Math.toRadians(latitude1);

        double latitude2Radians =
                Math.toRadians(latitude2);

        double haversine =
                Math.sin(latitudeDelta / 2)
                        * Math.sin(latitudeDelta / 2)
                        + Math.cos(latitude1Radians)
                        * Math.cos(latitude2Radians)
                        * Math.sin(longitudeDelta / 2)
                        * Math.sin(longitudeDelta / 2);

        double centralAngle =
                2 * Math.atan2(
                        Math.sqrt(haversine),
                        Math.sqrt(1 - haversine)
                );

        return (int) Math.round(
                EARTH_RADIUS_METERS * centralAngle
        );
    }

    private record RankedPlace(
            Place place,
            Integer distanceMeters,
            Boolean nearby
    ) {
    }
}

package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.repository.PlaceImageRepository;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HotPlaceRecommendationResponse;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HotPlaceRecommendationResponseAssembler {

    private static final int NEARBY_DISTANCE_METERS = 500;

    private final PlaceImageRepository placeImageRepository;

    public HotPlaceRecommendationResponse.Result assemble(
            RecommendationLog recommendationLog,
            CourseDraft courseDraft,
            boolean locationAvailable,
            List<RecommendationResult> results
    ) {
        Map<Long, String> imageUrlByPlaceId =
                loadPrimaryImageUrls(results);

        List<HotPlaceRecommendationResponse.PlaceItem> places =
                results.stream()
                        .map(result -> toPlaceItem(
                                result,
                                locationAvailable,
                                imageUrlByPlaceId
                        ))
                        .toList();

        return new HotPlaceRecommendationResponse.Result(
                recommendationLog.getId(),
                courseDraft.getId(),
                courseDraft.getStatus(),
                locationAvailable,
                places
        );
    }

    private HotPlaceRecommendationResponse.PlaceItem toPlaceItem(
            RecommendationResult result,
            boolean locationAvailable,
            Map<Long, String> imageUrlByPlaceId
    ) {
        Place place = result.getPlace();
        Integer distanceMeters = result.getDistanceMeters();

        Boolean nearby = locationAvailable
                ? distanceMeters != null
                    && distanceMeters <= NEARBY_DISTANCE_METERS
                : null;

        String roadAddress =
                place.getRoadAddress() == null
                        || place.getRoadAddress().isBlank()
                        ? place.getAddress()
                        : place.getRoadAddress();

        String imageUrl = imageUrlByPlaceId.getOrDefault(
                place.getId(),
                place.getDefaultImageUrl()
        );

        return new HotPlaceRecommendationResponse.PlaceItem(
                place.getId(),
                place.getName(),
                place.getAddress(),
                roadAddress,
                place.getLatitude(),
                place.getLongitude(),
                new HotPlaceRecommendationResponse.AreaInfo(
                        place.getArea().getId(),
                        place.getArea().getCode(),
                        place.getArea().getName()
                ),
                new HotPlaceRecommendationResponse.CategoryInfo(
                        place.getPlaceCategory().getId(),
                        place.getPlaceCategory().getCode(),
                        place.getPlaceCategory().getName()
                ),
                place.getSubCategory(),
                imageUrl,
                result.getRankNo(),
                distanceMeters,
                nearby,
                result.getMatchedMoodCount(),
                result.getMatchedFoodCount(),
                result.getReasonText(),
                true
        );
    }

    private Map<Long, String> loadPrimaryImageUrls(
            List<RecommendationResult> results
    ) {
        if (results.isEmpty()) {
            return Map.of();
        }

        List<Long> placeIds = results.stream()
                .map(RecommendationResult::getPlace)
                .map(Place::getId)
                .toList();

        Map<Long, String> imageUrlByPlaceId = new HashMap<>();

        for (PlaceImageRepository.PrimaryImageUrlView view
                : placeImageRepository
                .findPrimaryImageUrlsByPlaceIds(placeIds)) {
            imageUrlByPlaceId.putIfAbsent(
                    view.getPlaceId(),
                    view.getImageUrl()
            );
        }

        return imageUrlByPlaceId;
    }
}

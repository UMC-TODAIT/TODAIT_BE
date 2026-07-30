package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.place.dto.response.KakaoPlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceSource;
import com.example.TODAIT__BE.domain.place.enums.PlaceDataSourceCode;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceSearchImageType;
import com.example.TODAIT__BE.domain.place.repository.PlaceImageRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceSourceRepository;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KakaoPlaceSearchEnricher {

    private final KakaoPlaceAreaResolver areaResolver;
    private final KakaoPlaceCategoryResolver categoryResolver;
    private final PlaceSourceRepository placeSourceRepository;
    private final PlaceImageRepository placeImageRepository;

    public List<KakaoPlaceSearchResponse.PlaceItem> enrich(
            List<KakaoPlaceCandidate> candidates
    ) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<String, Area> activeAreasByCode =
                areaResolver.getActiveAreasByCode();

        Map<String, PlaceCategory> activeCategoriesByCode =
                categoryResolver.getActiveCategoriesByCode();

        Map<String, Place> registeredPlacesByExternalId =
                findRegisteredPlacesByExternalId(candidates);

        Map<Long, String> primaryImageUrlsByPlaceId =
                findPrimaryImageUrlsByPlaceId(
                        registeredPlacesByExternalId
                );
        Set<Long> operatorSourcePlaceIds =
                findOperatorSourcePlaceIds(
                        registeredPlacesByExternalId
                );

        return candidates.stream()
                .map(candidate -> toPlaceItem(
                        candidate,
                        activeAreasByCode,
                        activeCategoriesByCode,
                        registeredPlacesByExternalId,
                        primaryImageUrlsByPlaceId,
                        operatorSourcePlaceIds
                ))
                .filter(Objects::nonNull)
                .toList();
    }

    private KakaoPlaceSearchResponse.PlaceItem toPlaceItem(
            KakaoPlaceCandidate candidate,
            Map<String, Area> activeAreasByCode,
            Map<String, PlaceCategory> activeCategoriesByCode,
            Map<String, Place> registeredPlacesByExternalId,
            Map<Long, String> primaryImageUrlsByPlaceId,
            Set<Long> operatorSourcePlaceIds
    ) {
        Area area = areaResolver.resolve(
                candidate.address(),
                candidate.roadAddress(),
                activeAreasByCode
        );

        PlaceCategory category = categoryResolver.resolve(
                candidate,
                activeCategoriesByCode
        );

        if (area == null || category == null) {
            return null;
        }

        Place registeredPlace =
                registeredPlacesByExternalId.get(
                        candidate.externalPlaceId()
                );

        boolean isRegistered = registeredPlace != null;

        ImageSelection imageSelection =
                selectImage(
                        registeredPlace,
                        category,
                        primaryImageUrlsByPlaceId
                );

        boolean detailAvailable =
                isDetailAvailable(
                        registeredPlace,
                        operatorSourcePlaceIds
                );

        return new KakaoPlaceSearchResponse.PlaceItem(
                candidate.externalPlaceId(),
                isRegistered ? registeredPlace.getId() : null,
                candidate.name(),
                emptyToNull(candidate.address()),
                emptyToNull(candidate.roadAddress()),
                candidate.latitude(),
                candidate.longitude(),
                emptyToNull(candidate.phone()),
                emptyToNull(candidate.sourceUrl()),
                new KakaoPlaceSearchResponse.AreaInfo(
                        area.getId(),
                        area.getCode(),
                        area.getName()
                ),
                new KakaoPlaceSearchResponse.CategoryInfo(
                        category.getId(),
                        category.getCode(),
                        category.getName()
                ),
                extractSubCategory(candidate.categoryName()),
                isRegistered,
                imageSelection.imageUrl(),
                imageSelection.imageType(),
                detailAvailable
        );
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String extractSubCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }

        String[] categories = categoryName.split(">");

        return categories[categories.length - 1].trim();
    }


    private Map<String, Place> findRegisteredPlacesByExternalId(
            List<KakaoPlaceCandidate> candidates
    ) {
        if (candidates.isEmpty()) {
            return Map.of();
        }

        List<String> externalPlaceIds = candidates.stream()
                .map(KakaoPlaceCandidate::externalPlaceId)
                .toList();

        return placeSourceRepository
                .findRegisteredPlaceSources(
                        PlaceDataSourceCode.KAKAO.name(),
                        externalPlaceIds
                )
                .stream()
                .collect(
                        Collectors.toMap(
                                PlaceSource::getSourcePlaceId,
                                PlaceSource::getPlace,
                                (first, duplicate) -> first
                        )
                );
    }

    private Map<Long, String> findPrimaryImageUrlsByPlaceId(
            Map<String, Place> registeredPlacesByExternalId
    ) {
        List<Long> placeIds =
                registeredPlacesByExternalId.values()
                        .stream()
                        .map(Place::getId)
                        .distinct()
                        .toList();

        if (placeIds.isEmpty()) {
            return Map.of();
        }

        return placeImageRepository
                .findPrimaryImageUrlsByPlaceIds(placeIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                PlaceImageRepository
                                        .PrimaryImageUrlView
                                        ::getPlaceId,
                                PlaceImageRepository
                                        .PrimaryImageUrlView
                                        ::getImageUrl,
                                (first, duplicate) -> first
                        )
                );
    }

    private record ImageSelection(
            String imageUrl,
            PlaceSearchImageType imageType
    ) {
    }

    private ImageSelection selectImage(
            Place registeredPlace,
            PlaceCategory category,
            Map<Long, String> primaryImageUrlsByPlaceId
    ) {
        if (registeredPlace != null) {
            String primaryImageUrl =
                    primaryImageUrlsByPlaceId.get(
                            registeredPlace.getId()
                    );

            if (hasText(primaryImageUrl)) {
                return new ImageSelection(
                        primaryImageUrl,
                        PlaceSearchImageType.PLACE_IMAGE
                );
            }

            if (hasText(registeredPlace.getDefaultImageUrl())) {
                return new ImageSelection(
                        registeredPlace.getDefaultImageUrl().trim(),
                        PlaceSearchImageType.PLACE_IMAGE
                );
            }
        }

        return new ImageSelection(
                PlaceCategoryDefaultImage.getImageUrl(
                        category.getCode()
                ),
                PlaceSearchImageType.CATEGORY_DEFAULT
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Set<Long> findOperatorSourcePlaceIds(
            Map<String, Place> registeredPlacesByExternalId
    ) {
        List<Long> placeIds =
                registeredPlacesByExternalId.values()
                        .stream()
                        .map(Place::getId)
                        .distinct()
                        .toList();

        if (placeIds.isEmpty()) {
            return Set.of();
        }

        return placeSourceRepository
                .findPlaceIdsHavingActiveDataSource(
                        placeIds,
                        PlaceDataSourceCode.OPERATOR.name()
                );
    }

    private boolean isDetailAvailable(
            Place place,
            Set<Long> operatorSourcePlaceIds
    ) {
        if (place == null) {
            return false;
        }

        return operatorSourcePlaceIds.contains(place.getId())
                && Boolean.TRUE.equals(place.getIsActive())
                && place.getReviewStatus() == PlaceReviewStatus.APPROVED
                && place.getExposureStatus() == PlaceExposureStatus.ACTIVE
                && place.getDeletedAt() == null
                && hasRequiredDetailData(place);
    }

    private boolean hasRequiredDetailData(Place place) {
        return hasText(place.getName())
                && hasText(place.getAddress())
                && place.getLatitude() != null
                && place.getLongitude() != null
                && place.getArea() != null
                && place.getPlaceCategory() != null;
    }
}

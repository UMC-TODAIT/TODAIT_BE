package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.place.dto.response.KakaoPlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KakaoPlaceSearchEnricher {

    private final KakaoPlaceAreaResolver areaResolver;
    private final KakaoPlaceCategoryResolver categoryResolver;
    private final KakaoPlaceSearchDataLoader dataLoader;
    private final PlaceSearchImageResolver imageResolver;
    private final PlaceDetailAvailabilityPolicy detailAvailabilityPolicy;

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

        KakaoPlaceSearchData searchData =
                dataLoader.load(candidates);

        return candidates.stream()
                .map(candidate -> toPlaceItem(
                        candidate,
                        activeAreasByCode,
                        activeCategoriesByCode,
                        searchData
                ))
                .filter(Objects::nonNull)
                .toList();
    }

    private KakaoPlaceSearchResponse.PlaceItem toPlaceItem(
            KakaoPlaceCandidate candidate,
            Map<String, Area> activeAreasByCode,
            Map<String, PlaceCategory> activeCategoriesByCode,
            KakaoPlaceSearchData searchData
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
                searchData.registeredPlacesByExternalId().get(
                        candidate.externalPlaceId()
                );

        boolean isRegistered = registeredPlace != null;

        PlaceSearchImageResolver.ImageSelection imageSelection =
                imageResolver.resolve(
                        registeredPlace,
                        category,
                        searchData.primaryImageUrlsByPlaceId()
                );

        boolean detailAvailable =
                detailAvailabilityPolicy.isAvailable(
                        registeredPlace,
                        searchData.operatorSourcePlaceIds()
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
}

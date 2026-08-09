package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceCandidate;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PlaceSearchEnricher {

    private final PlaceSearchAreaResolver areaResolver;
    private final PlaceSearchCategoryResolver categoryResolver;
    private final PlaceSearchDataLoader dataLoader;
    private final PlaceSearchImageResolver imageResolver;
    private final PlaceDetailAvailabilityPolicy detailAvailabilityPolicy;

    public List<PlaceSearchResponse.PlaceItem> enrich(
            List<ExternalPlaceCandidate> candidates
    ) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<String, Area> activeAreasByCode = areaResolver.getActiveAreasByCode();
        Map<String, PlaceCategory> activeCategoriesByCode =
                categoryResolver.getActiveCategoriesByCode();
        PlaceSearchData searchData = dataLoader.load(candidates);

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

    private PlaceSearchResponse.PlaceItem toPlaceItem(
            ExternalPlaceCandidate candidate,
            Map<String, Area> activeAreasByCode,
            Map<String, PlaceCategory> activeCategoriesByCode,
            PlaceSearchData searchData
    ) {
        Area area = areaResolver.resolve(candidate.areaCode(), activeAreasByCode);
        PlaceCategory category = categoryResolver.resolve(
                candidate.placeCategoryCode(),
                activeCategoriesByCode
        );

        if (area == null || category == null) {
            return null;
        }

        Place registeredPlace = searchData.registeredPlacesByExternalId()
                .get(candidate.externalPlaceId());
        boolean isRegistered = registeredPlace != null;

        PlaceSearchImageResolver.ImageSelection imageSelection = imageResolver.resolve(
                registeredPlace,
                category,
                searchData.primaryImageUrlsByPlaceId()
        );
        boolean detailAvailable = detailAvailabilityPolicy.isAvailable(
                registeredPlace,
                searchData.operatorSourcePlaceIds()
        );

        return new PlaceSearchResponse.PlaceItem(
                candidate.externalPlaceId(),
                isRegistered ? registeredPlace.getId() : null,
                candidate.name(),
                emptyToNull(candidate.address()),
                emptyToNull(candidate.roadAddress()),
                candidate.latitude(),
                candidate.longitude(),
                emptyToNull(candidate.phone()),
                emptyToNull(candidate.sourceUrl()),
                new PlaceSearchResponse.AreaInfo(area.getId(), area.getCode(), area.getName()),
                new PlaceSearchResponse.CategoryInfo(
                        category.getId(),
                        category.getCode(),
                        category.getName()
                ),
                emptyToNull(candidate.subCategory()),
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
}
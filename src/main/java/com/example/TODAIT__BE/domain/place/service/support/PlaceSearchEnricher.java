package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.place.code.PlaceSearchErrorCode;

import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceCandidate;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.repository.AreaRepository;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaceSearchEnricher {

    private final AreaRepository areaRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final PlaceSearchDataLoader dataLoader;
    private final PlaceSearchImageResolver imageResolver;

    public List<PlaceSearchResponse.PlaceItem> enrich(
            List<ExternalPlaceCandidate> candidates
    ) {
        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<String, Area> activeAreasByCode = getActiveAreasByCode();
        Map<String, PlaceCategory> activeCategoriesByCode = getActiveCategoriesByCode();
        PlaceSearchDataLoader.SearchData searchData = dataLoader.load(candidates);

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
            PlaceSearchDataLoader.SearchData searchData
    ) {
        Area area = resolveArea(candidate.areaCode(), activeAreasByCode);

        if (area == null) {
            return null;
        }

        PlaceCategory category = resolveCategory(
                candidate.placeCategoryCode(),
                activeCategoriesByCode
        );

        Place registeredPlace = searchData.registeredPlacesByExternalId()
                .get(candidate.externalPlaceId());
        boolean isRegistered = registeredPlace != null;

        PlaceSearchImageResolver.ImageSelection imageSelection = imageResolver.resolve(
                registeredPlace,
                searchData.primaryImageUrlsByPlaceId()
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
                isDetailAvailable(registeredPlace, searchData.operatorSourcePlaceIds())
        );
    }

    private Map<String, Area> getActiveAreasByCode() {
        return areaRepository
                .findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .collect(Collectors.toMap(Area::getCode, Function.identity()));
    }

    private Map<String, PlaceCategory> getActiveCategoriesByCode() {
        return placeCategoryRepository
                .findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .collect(Collectors.toMap(PlaceCategory::getCode, Function.identity()));
    }

    private Area resolveArea(
            String areaCode,
            Map<String, Area> activeAreasByCode
    ) {
        if (areaCode == null || areaCode.isBlank()) {
            return null;
        }

        return activeAreasByCode.get(areaCode.trim());
    }

    private PlaceCategory resolveCategory(
            String placeCategoryCode,
            Map<String, PlaceCategory> activeCategoriesByCode
    ) {
        if (placeCategoryCode == null || placeCategoryCode.isBlank()) {
            throw new PlaceException(
                    PlaceSearchErrorCode.PLACE_CATEGORY_CONFIGURATION_MISSING
            );
        }

        PlaceCategory category = activeCategoriesByCode.get(placeCategoryCode.trim());

        if (category == null) {
            throw new PlaceException(
                    PlaceSearchErrorCode.PLACE_CATEGORY_CONFIGURATION_MISSING
            );
        }

        return category;
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceSource;
import com.example.TODAIT__BE.domain.place.enums.PlaceDataSourceCode;
import com.example.TODAIT__BE.domain.place.repository.PlaceImageRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceSourceRepository;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceCandidate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaceSearchDataLoader {

    private final PlaceSourceRepository placeSourceRepository;
    private final PlaceImageRepository placeImageRepository;

    public SearchData load(
            List<ExternalPlaceCandidate> candidates
    ) {
        Map<String, Place> registeredPlacesByExternalId =
                findRegisteredPlacesByExternalId(candidates);

        List<Long> registeredPlaceIds =
                registeredPlacesByExternalId.values()
                        .stream()
                        .map(Place::getId)
                        .distinct()
                        .toList();

        return new SearchData(
                registeredPlacesByExternalId,
                findPrimaryImageUrlsByPlaceId(registeredPlaceIds),
                findOperatorSourcePlaceIds(registeredPlaceIds)
        );
    }

    private Map<String, Place> findRegisteredPlacesByExternalId(
            List<ExternalPlaceCandidate> candidates
    ) {
        if (candidates.isEmpty()) {
            return Map.of();
        }

        PlaceDataSourceCode source = candidates.get(0).source();
        boolean containsDifferentSource = candidates.stream()
                .anyMatch(candidate -> candidate.source() != source);

        if (containsDifferentSource) {
            throw new IllegalStateException(
                    "한 번의 장소 검색 결과에는 하나의 외부 출처만 포함할 수 있습니다."
            );
        }

        List<String> externalPlaceIds = candidates.stream()
                .map(ExternalPlaceCandidate::externalPlaceId)
                .toList();

        return placeSourceRepository
                .findRegisteredPlaceSources(source.name(), externalPlaceIds)
                .stream()
                .collect(Collectors.toMap(
                        PlaceSource::getSourcePlaceId,
                        PlaceSource::getPlace
                ));
    }

    private Map<Long, String> findPrimaryImageUrlsByPlaceId(
            List<Long> placeIds
    ) {
        if (placeIds.isEmpty()) {
            return Map.of();
        }

        return placeImageRepository
                .findPrimaryImageUrlsByPlaceIds(placeIds)
                .stream()
                .collect(Collectors.toMap(
                        PlaceImageRepository.PrimaryImageUrlView::getPlaceId,
                        PlaceImageRepository.PrimaryImageUrlView::getImageUrl,
                        (first, duplicate) -> first
                ));
    }

    private Set<Long> findOperatorSourcePlaceIds(
            List<Long> placeIds
    ) {
        if (placeIds.isEmpty()) {
            return Set.of();
        }

        return placeSourceRepository.findPlaceIdsHavingActiveDataSource(
                placeIds,
                PlaceDataSourceCode.OPERATOR.name()
        );
    }

    public record SearchData(
            Map<String, Place> registeredPlacesByExternalId,
            Map<Long, String> primaryImageUrlsByPlaceId,
            Set<Long> operatorSourcePlaceIds
    ) {
    }
}

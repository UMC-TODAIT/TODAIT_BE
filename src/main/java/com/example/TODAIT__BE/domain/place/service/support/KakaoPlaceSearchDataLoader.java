package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceSource;
import com.example.TODAIT__BE.domain.place.enums.PlaceDataSourceCode;
import com.example.TODAIT__BE.domain.place.repository.PlaceImageRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KakaoPlaceSearchDataLoader {

    private final PlaceSourceRepository placeSourceRepository;
    private final PlaceImageRepository placeImageRepository;

    public KakaoPlaceSearchData load(
            List<KakaoPlaceCandidate> candidates
    ) {
        Map<String, Place> registeredPlacesByExternalId =
                findRegisteredPlacesByExternalId(candidates);

        List<Long> registeredPlaceIds =
                registeredPlacesByExternalId.values()
                        .stream()
                        .map(Place::getId)
                        .distinct()
                        .toList();

        return new KakaoPlaceSearchData(
                registeredPlacesByExternalId,
                findPrimaryImageUrlsByPlaceId(registeredPlaceIds),
                findOperatorSourcePlaceIds(registeredPlaceIds)
        );
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
                                PlaceSource::getPlace
                        )
                );
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

    private Set<Long> findOperatorSourcePlaceIds(
            List<Long> placeIds
    ) {
        if (placeIds.isEmpty()) {
            return Set.of();
        }

        return placeSourceRepository
                .findPlaceIdsHavingActiveDataSource(
                        placeIds,
                        PlaceDataSourceCode.OPERATOR.name()
                );
    }
}

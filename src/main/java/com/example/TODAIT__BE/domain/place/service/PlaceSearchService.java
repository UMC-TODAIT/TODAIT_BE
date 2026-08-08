package com.example.TODAIT__BE.domain.place.service;

import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceCandidate;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceSearchResult;
import com.example.TODAIT__BE.domain.place.service.port.PlaceSearchPort;
import com.example.TODAIT__BE.domain.place.service.support.PlaceSearchEnricher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private static final int EXTERNAL_SEARCH_SIZE = 15;
    private static final int MAX_EXTERNAL_SEARCH_PAGES = 3;
    private static final int MAX_RESULT_COUNT = 10;
    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_QUERY_LENGTH = 100;

    private final PlaceSearchPort placeSearchPort;
    private final PlaceSearchEnricher searchEnricher;

    @Transactional(readOnly = true)
    public PlaceSearchResponse.SearchResult search(String query) {
        String normalizedQuery =
                validateAndNormalizeQuery(query);

        List<PlaceSearchResponse.PlaceItem> places =
                new ArrayList<>();

        Set<String> seenExternalPlaceIds =
                new HashSet<>();

        for (int page = 1;
             page <= MAX_EXTERNAL_SEARCH_PAGES
                     && places.size() < MAX_RESULT_COUNT;
             page++) {

            ExternalPlaceSearchResult searchResult =
                    placeSearchPort.searchByKeyword(
                            normalizedQuery,
                            page,
                            EXTERNAL_SEARCH_SIZE
                    );

            List<ExternalPlaceCandidate> newCandidates =
                    searchResult.candidates().stream()
                            .filter(candidate ->
                                    seenExternalPlaceIds.add(
                                            candidate.externalPlaceId()
                                    )
                            )
                            .toList();

            List<PlaceSearchResponse.PlaceItem> enrichedPlaces =
                    searchEnricher.enrich(newCandidates);

            enrichedPlaces.stream()
                    .limit(MAX_RESULT_COUNT - places.size())
                    .forEach(places::add);

            if (searchResult.end()) {
                break;
            }
        }

        return new PlaceSearchResponse.SearchResult(
                normalizedQuery,
                places.size(),
                places
        );
    }

    private String validateAndNormalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new PlaceException(
                    PlaceErrorCode.INVALID_PLACE_SEARCH_QUERY
            );
        }

        String normalizedQuery = query.trim();

        if (normalizedQuery.length() < MIN_QUERY_LENGTH) {
            throw new PlaceException(
                    PlaceErrorCode.PLACE_SEARCH_QUERY_TOO_SHORT
            );
        }

        if (normalizedQuery.length() > MAX_QUERY_LENGTH) {
            throw new PlaceException(
                    PlaceErrorCode.PLACE_SEARCH_QUERY_TOO_LONG
            );
        }

        return normalizedQuery;
    }
}

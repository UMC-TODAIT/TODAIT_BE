package com.example.TODAIT__BE.domain.place.service;

import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.port.out.ExternalPlaceCandidate;
import com.example.TODAIT__BE.domain.place.port.out.PlaceSearchPort;
import com.example.TODAIT__BE.domain.place.service.support.PlaceSearchEnricher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private static final int EXTERNAL_SEARCH_PAGE = 1;
    private static final int EXTERNAL_SEARCH_SIZE = 15;
    private static final int MAX_RESULT_COUNT = 10;
    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_QUERY_LENGTH = 100;

    private final PlaceSearchPort placeSearchPort;
    private final PlaceSearchEnricher searchEnricher;

    @Transactional(readOnly = true)
    public PlaceSearchResponse.SearchResult search(String query) {
        String normalizedQuery = validateAndNormalizeQuery(query);

        List<ExternalPlaceCandidate> candidates =
                placeSearchPort.searchByKeyword(
                        normalizedQuery,
                        EXTERNAL_SEARCH_PAGE,
                        EXTERNAL_SEARCH_SIZE
                );

        List<PlaceSearchResponse.PlaceItem> places =
                searchEnricher.enrich(candidates)
                        .stream()
                        .limit(MAX_RESULT_COUNT)
                        .toList();

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

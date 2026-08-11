package com.example.TODAIT__BE.domain.place.service;

import com.example.TODAIT__BE.domain.place.code.PlaceSearchErrorCode;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceCandidate;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceSearchResult;
import com.example.TODAIT__BE.domain.place.service.port.PlaceSearchPort;
import com.example.TODAIT__BE.domain.place.service.support.PlaceSearchEnricher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceSearchService {

    private static final int DEFAULT_CURSOR = 1;
    private static final int MAX_CURSOR = 45;
    private static final int DEFAULT_SIZE = 10;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 15;
    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_QUERY_LENGTH = 100;

    private final PlaceSearchPort placeSearchPort;
    private final PlaceSearchEnricher searchEnricher;

    @Transactional(readOnly = true)
    public PlaceSearchResponse.SearchResult search(
            String query,
            Integer cursor,
            Integer size
    ) {
        String normalizedQuery =
                validateAndNormalizeQuery(query);
        int resolvedCursor = validateAndResolveCursor(cursor);
        int resolvedSize = validateAndResolveSize(size);

        ExternalPlaceSearchResult searchResult =
                placeSearchPort.searchByKeyword(
                        normalizedQuery,
                        resolvedCursor,
                        resolvedSize
                );

        List<ExternalPlaceCandidate> candidates = searchResult.candidates();
        List<PlaceSearchResponse.PlaceItem> places = searchEnricher.enrich(candidates);
        boolean hasNext = !searchResult.end();

        return new PlaceSearchResponse.SearchResult(
                normalizedQuery,
                places.size(),
                hasNext ? resolvedCursor + 1 : null,
                hasNext,
                places
        );
    }

    private int validateAndResolveCursor(Integer cursor) {
        int resolvedCursor = cursor == null ? DEFAULT_CURSOR : cursor;
        if (resolvedCursor < DEFAULT_CURSOR || resolvedCursor > MAX_CURSOR) {
            throw new PlaceException(
                    PlaceSearchErrorCode.INVALID_PLACE_SEARCH_CURSOR
            );
        }
        return resolvedCursor;
    }

    private int validateAndResolveSize(Integer size) {
        int resolvedSize = size == null ? DEFAULT_SIZE : size;
        if (resolvedSize < MIN_SIZE || resolvedSize > MAX_SIZE) {
            throw new PlaceException(
                    PlaceSearchErrorCode.INVALID_PLACE_SEARCH_SIZE
            );
        }
        return resolvedSize;
    }

    private String validateAndNormalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new PlaceException(
                    PlaceSearchErrorCode.INVALID_PLACE_SEARCH_QUERY
            );
        }

        String normalizedQuery = query.trim();

        if (normalizedQuery.length() < MIN_QUERY_LENGTH) {
            throw new PlaceException(
                    PlaceSearchErrorCode.PLACE_SEARCH_QUERY_TOO_SHORT
            );
        }

        if (normalizedQuery.length() > MAX_QUERY_LENGTH) {
            throw new PlaceException(
                    PlaceSearchErrorCode.PLACE_SEARCH_QUERY_TOO_LONG
            );
        }

        return normalizedQuery;
    }
}

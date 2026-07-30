package com.example.TODAIT__BE.domain.place.service;

import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.dto.response.KakaoPlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.service.support.KakaoPlaceCandidate;
import com.example.TODAIT__BE.domain.place.service.support.KakaoPlaceCandidateMapper;
import com.example.TODAIT__BE.domain.place.service.support.KakaoPlaceSearchEnricher;
import com.example.TODAIT__BE.infra.kakao.local.KakaoLocalClient;
import com.example.TODAIT__BE.infra.kakao.local.dto.KakaoKeywordSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class KakaoPlaceSearchService {
    private static final int KAKAO_PAGE = 1;
    private static final int KAKAO_SIZE = 15;
    private static final int MAX_RESULT_COUNT = 10;

    private final KakaoLocalClient kakaoLocalClient;
    private final KakaoPlaceCandidateMapper candidateMapper;
    private final KakaoPlaceSearchEnricher searchEnricher;

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_QUERY_LENGTH = 100;

    @Transactional(readOnly = true)
    public KakaoPlaceSearchResponse.SearchResult search(
            String query
    ) {
        String normalizedQuery =
                validateAndNormalizeQuery(query);

        KakaoKeywordSearchResponse.Result kakaoResponse =
                kakaoLocalClient.searchByKeyword(
                        normalizedQuery,
                        KAKAO_PAGE,
                        KAKAO_SIZE
                );

        List<KakaoPlaceCandidate> candidates =
                candidateMapper.map(kakaoResponse);

        List<KakaoPlaceSearchResponse.PlaceItem> places =
                searchEnricher.enrich(candidates)
                        .stream()
                        .limit(MAX_RESULT_COUNT)
                        .toList();

        return new KakaoPlaceSearchResponse.SearchResult(
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

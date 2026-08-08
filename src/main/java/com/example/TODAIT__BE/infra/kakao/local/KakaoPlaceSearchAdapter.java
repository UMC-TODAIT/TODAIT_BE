package com.example.TODAIT__BE.infra.kakao.local;

import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.enums.PlaceDataSourceCode;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceCandidate;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceSearchResult;
import com.example.TODAIT__BE.domain.place.service.port.PlaceSearchPort;
import com.example.TODAIT__BE.infra.kakao.local.dto.KakaoKeywordSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoPlaceSearchAdapter implements PlaceSearchPort {

    private final KakaoLocalClient kakaoLocalClient;

    @Override
    public ExternalPlaceSearchResult searchByKeyword(
            String query,
            int page,
            int size
    ) {
        KakaoKeywordSearchResponse.Result response =
                kakaoLocalClient.searchByKeyword(
                        query,
                        page,
                        size
                );

        if (response.meta() == null) {
            throw new PlaceException(
                    PlaceErrorCode.KAKAO_LOCAL_API_REQUEST_FAILED
            );
        }

        Map<String, ExternalPlaceCandidate> uniqueCandidates =
                new LinkedHashMap<>();

        for (KakaoKeywordSearchResponse.Document document
                : getDocuments(response)) {
            ExternalPlaceCandidate candidate = toCandidate(document);

            if (candidate == null) {
                continue;
            }

            uniqueCandidates.putIfAbsent(
                    candidate.externalPlaceId(),
                    candidate
            );
        }


        return new ExternalPlaceSearchResult(
                new ArrayList<>(uniqueCandidates.values()),
                response.meta().end()
        );
    }

    private ExternalPlaceCandidate toCandidate(
            KakaoKeywordSearchResponse.Document document
    ) {
        if (document == null
                || document.id() == null
                || document.id().isBlank()
                || document.addressName() == null
                || document.addressName().isBlank()) {
            return null;
        }

        BigDecimal latitude = parseCoordinate(document.y());
        BigDecimal longitude = parseCoordinate(document.x());

        if (!isValidCoordinate(latitude, longitude)) {
            return null;
        }

        return new ExternalPlaceCandidate(
                PlaceDataSourceCode.KAKAO,
                document.id(),
                document.placeName(),
                document.categoryName(),
                document.categoryGroupCode(),
                document.categoryGroupName(),
                document.phone(),
                document.addressName(),
                document.roadAddressName(),
                latitude,
                longitude,
                document.placeUrl()
        );
    }

    private List<KakaoKeywordSearchResponse.Document> getDocuments(
            KakaoKeywordSearchResponse.Result response
    ) {
        if (response == null || response.documents() == null) {
            return List.of();
        }

        return response.documents();
    }

    private BigDecimal parseCoordinate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isValidCoordinate(
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        if (latitude == null || longitude == null) {
            return false;
        }

        boolean validLatitude =
                latitude.compareTo(BigDecimal.valueOf(-90)) >= 0
                        && latitude.compareTo(BigDecimal.valueOf(90)) <= 0;

        boolean validLongitude =
                longitude.compareTo(BigDecimal.valueOf(-180)) >= 0
                        && longitude.compareTo(BigDecimal.valueOf(180)) <= 0;

        return validLatitude && validLongitude;
    }
}

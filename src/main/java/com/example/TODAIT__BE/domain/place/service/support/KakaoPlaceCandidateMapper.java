package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.infra.kakao.local.dto.KakaoKeywordSearchResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KakaoPlaceCandidateMapper {

    public List<KakaoPlaceCandidate> map(
            KakaoKeywordSearchResponse.Result response
    ) {
        Map<String, KakaoPlaceCandidate> uniqueCandidates =
                new LinkedHashMap<>();

        for (KakaoKeywordSearchResponse.Document document : getDocuments(response)) {

            KakaoPlaceCandidate candidate = toCandidate(document);

            if (candidate == null) {
                continue;
            }

            uniqueCandidates.putIfAbsent(
                    candidate.externalPlaceId(),
                    candidate
            );
        }

        return new ArrayList<>(uniqueCandidates.values());
    }

    private KakaoPlaceCandidate toCandidate(
            KakaoKeywordSearchResponse.Document document
    ) {
        if (document == null
                || document.id() == null
                || document.id().isBlank()
                || document.addressName() == null
                || document.addressName().isBlank()) {
            return null;
        }

        BigDecimal latitude =
                parseCoordinate(document.y());

        BigDecimal longitude =
                parseCoordinate(document.x());

        if (!isValidCoordinate(latitude, longitude)) {
            return null;
        }

        return new KakaoPlaceCandidate(
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
                        && latitude.compareTo(
                        BigDecimal.valueOf(90)
                ) <= 0;

        boolean validLongitude =
                longitude.compareTo(
                        BigDecimal.valueOf(-180)
                ) >= 0
                        && longitude.compareTo(
                        BigDecimal.valueOf(180)
                ) <= 0;

        return validLatitude && validLongitude;
    }
}

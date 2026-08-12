package com.example.TODAIT__BE.infra.kakao.local;

import com.example.TODAIT__BE.domain.place.code.PlaceSearchErrorCode;
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

    private static final String FOOD_GROUP_CODE = "FD6";
    private static final String CAFE_GROUP_CODE = "CE7";

    private static final List<AreaRule> AREA_RULES = List.of(
            new AreaRule("HONGDAE", List.of("서교동", "동교동", "합정동", "상수동")),
            new AreaRule("YEONNAM", List.of("연남동")),
            new AreaRule("SEONGSU", List.of("성수동1가", "성수동2가"))
    );

    private static final List<String> BAR_KEYWORDS = List.of(
            "술집", "요리주점", "이자카야", "와인바", "칵테일바", "맥주", "호프"
    );

    private static final List<String> ACTIVITY_KEYWORDS = List.of(
            "방탈출", "보드게임", "공방", "클라이밍", "볼링", "VR"
    );

    private final KakaoLocalClient kakaoLocalClient;

    @Override
    public ExternalPlaceSearchResult searchByKeyword(
            String query,
            int page,
            int size
    ) {
        KakaoKeywordSearchResponse.Result response = kakaoLocalClient.searchByKeyword(
                query,
                page,
                size
        );

        if (response.meta() == null) {
            throw new PlaceException(PlaceSearchErrorCode.KAKAO_LOCAL_API_REQUEST_FAILED);
        }

        Map<String, ExternalPlaceCandidate> uniqueCandidates = new LinkedHashMap<>();

        for (KakaoKeywordSearchResponse.Document document : getDocuments(response)) {
            ExternalPlaceCandidate candidate = toCandidate(document);

            if (candidate == null) {
                continue;
            }

            uniqueCandidates.putIfAbsent(candidate.externalPlaceId(), candidate);
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
                document.addressName(),
                document.roadAddressName(),
                latitude,
                longitude,
                document.phone(),
                document.placeUrl(),
                determineAreaCode(document.addressName(), document.roadAddressName()),
                determinePlaceCategoryCode(document.categoryGroupCode(), document.categoryName()),
                extractSubCategory(document.categoryName())
        );
    }

    private String determineAreaCode(
            String address,
            String roadAddress
    ) {
        String addressText = normalize(address) + " " + normalize(roadAddress);

        for (AreaRule rule : AREA_RULES) {
            if (containsAnyKeyword(addressText, rule.addressKeywords())) {
                return rule.areaCode();
            }
        }

        return null;
    }

    private String determinePlaceCategoryCode(
            String categoryGroupCode,
            String categoryName
    ) {
        String groupCode = normalize(categoryGroupCode);
        String normalizedCategoryName = normalize(categoryName);

        if (FOOD_GROUP_CODE.equals(groupCode) && containsAnyKeyword(normalizedCategoryName, BAR_KEYWORDS)) {
            return "BAR";
        }

        if (CAFE_GROUP_CODE.equals(groupCode)) {
            return "CAFE";
        }

        if (FOOD_GROUP_CODE.equals(groupCode)) {
            return "RESTAURANT";
        }

        if (containsAnyKeyword(normalizedCategoryName, ACTIVITY_KEYWORDS)) {
            return "ACTIVITY";
        }

        return "OTHER";
    }

    private String extractSubCategory(String categoryName) {
        String normalizedCategoryName = normalize(categoryName);

        if (normalizedCategoryName.isBlank()) {
            return null;
        }

        String[] categories = normalizedCategoryName.split(">");
        return categories[categories.length - 1].trim();
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

        boolean validLatitude = latitude.compareTo(BigDecimal.valueOf(-90)) >= 0
                && latitude.compareTo(BigDecimal.valueOf(90)) <= 0;
        boolean validLongitude = longitude.compareTo(BigDecimal.valueOf(-180)) >= 0
                && longitude.compareTo(BigDecimal.valueOf(180)) <= 0;

        return validLatitude && validLongitude;
    }

    private boolean containsAnyKeyword(
            String text,
            List<String> keywords
    ) {
        return keywords.stream().anyMatch(text::contains);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record AreaRule(
            String areaCode,
            List<String> addressKeywords
    ) {
    }
}

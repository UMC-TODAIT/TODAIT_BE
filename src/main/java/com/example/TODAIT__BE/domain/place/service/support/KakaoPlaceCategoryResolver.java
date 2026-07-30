package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KakaoPlaceCategoryResolver {
    private static final String FOOD_GROUP_CODE = "FD6";
    private static final String CAFE_GROUP_CODE = "CE7";
    private static final String CULTURE_GROUP_CODE = "CT1";
    private static final String ATTRACTION_GROUP_CODE = "AT4";

    private static final List<String> BAR_KEYWORDS = List.of(
            "술집",
            "요리주점",
            "이자카야",
            "와인바",
            "칵테일바",
            "맥주",
            "호프"
    );

    private static final List<String> ACTIVITY_KEYWORDS = List.of(
            "방탈출",
            "보드게임",
            "공방",
            "클라이밍",
            "볼링",
            "VR"
    );

    private final PlaceCategoryRepository placeCategoryRepository;

    public Map<String, PlaceCategory> getActiveCategoriesByCode() {
        return placeCategoryRepository
                .findAllByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .collect(
                        Collectors.toMap(
                                PlaceCategory::getCode,
                                Function.identity()
                        )
                );
    }

    public PlaceCategory resolve(
            KakaoPlaceCandidate candidate,
            Map<String, PlaceCategory> activeCategoriesByCode
    ) {
        String categoryCode = determineCategoryCode(candidate);

        if (categoryCode == null) {
            return null;
        }

        return activeCategoriesByCode.get(categoryCode);
    }

    private String determineCategoryCode(
            KakaoPlaceCandidate candidate
    ) {
        String groupCode =
                normalize(candidate.categoryGroupCode());

        String categoryName =
                normalize(candidate.categoryName());

        if (FOOD_GROUP_CODE.equals(groupCode)
                && containsAnyKeyword(
                categoryName,
                BAR_KEYWORDS
        )) {
            return "BAR";
        }

        if (CAFE_GROUP_CODE.equals(groupCode)) {
            return "CAFE";
        }

        if (FOOD_GROUP_CODE.equals(groupCode)) {
            return "RESTAURANT";
        }

        if (isActivity(
                groupCode,
                categoryName
        )) {
            return "ACTIVITY";
        }

        return null;
    }

    private boolean isActivity(
            String groupCode,
            String categoryName
    ) {
        boolean supportedGroup =
                CULTURE_GROUP_CODE.equals(groupCode)
                        || ATTRACTION_GROUP_CODE.equals(groupCode);

        return supportedGroup
                && containsAnyKeyword(
                categoryName,
                ACTIVITY_KEYWORDS
        );
    }

    private boolean containsAnyKeyword(
            String text,
            List<String> keywords
    ) {
        return keywords.stream()
                .anyMatch(text::contains);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

}

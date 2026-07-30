package com.example.TODAIT__BE.domain.place.dto.response;
import com.example.TODAIT__BE.domain.place.enums.PlaceSearchImageType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public final class KakaoPlaceSearchResponse {

    private KakaoPlaceSearchResponse(){}

    public record SearchResult(
            String query,
            int resultCount,
            List<PlaceItem> places

    ){}

    public record PlaceItem(
            String externalPlaceId,
            Long placeId,
            String name,
            String address,
            String roadAddress,
            BigDecimal latitude,
            BigDecimal longitude,
            String phone,
            String sourceUrl,
            AreaInfo area,
            CategoryInfo category,
            String subCategory,
            @JsonProperty("isRegistered")
            boolean isRegistered,
            String imageUrl,
            PlaceSearchImageType imageType,
            boolean detailAvailable
    ){}

    public record AreaInfo(
            Long areaId,
            String code,
            String name
    ){}

    public record CategoryInfo(
            Long placeCategoryId,
            String code,
            String name
    ) {}
}

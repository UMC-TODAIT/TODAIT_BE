package com.example.TODAIT__BE.domain.place.service.port;

import com.example.TODAIT__BE.domain.place.enums.PlaceDataSourceCode;

import java.math.BigDecimal;

public record ExternalPlaceCandidate(
        PlaceDataSourceCode source,
        String externalPlaceId,
        String name,
        String address,
        String roadAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        String phone,
        String sourceUrl,
        String areaCode,
        String placeCategoryCode,
        String subCategory
) {
}

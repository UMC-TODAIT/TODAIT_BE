package com.example.TODAIT__BE.domain.place.port.out;

import com.example.TODAIT__BE.domain.place.enums.PlaceDataSourceCode;

import java.math.BigDecimal;

public record ExternalPlaceCandidate(
        PlaceDataSourceCode source,
        String externalPlaceId,
        String name,
        String categoryName,
        String categoryGroupCode,
        String categoryGroupName,
        String phone,
        String address,
        String roadAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        String sourceUrl
) {
}

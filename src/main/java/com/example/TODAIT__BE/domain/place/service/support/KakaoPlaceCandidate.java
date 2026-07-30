package com.example.TODAIT__BE.domain.place.service.support;

import java.math.BigDecimal;

public record KakaoPlaceCandidate(
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

package com.example.TODAIT__BE.domain.place.service.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum PlaceCategoryDefaultImage {

    CAFE(null),
    RESTAURANT(null),
    BAR(null),
    ACTIVITY(null);

    private static final Logger log =
            LoggerFactory.getLogger(PlaceCategoryDefaultImage.class);

    private final String imageUrl;

    PlaceCategoryDefaultImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.startsWith("https://")) {
            throw new IllegalArgumentException(
                    "카테고리 기본 이미지는 공개 HTTPS URL이어야 합니다."
            );
        }
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public static boolean isSupported(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return false;
        }

        try {
            valueOf(categoryCode.trim());
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static String getImageUrl(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return null;
        }

        String normalizedCode = categoryCode.trim();

        try {
            return valueOf(normalizedCode).imageUrl;
        } catch (IllegalArgumentException exception) {
            log.warn(
                    "Unknown place category code for default image: {}",
                    categoryCode
            );
            return null;
        }
    }
}

package com.example.TODAIT__BE.domain.place.service.support;

public enum PlaceCategoryDefaultImage {

    CAFE(null),
    RESTAURANT(null),
    BAR(null),
    ACTIVITY(null);

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

    public static String getImageUrl(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            return null;
        }

        try {
            return valueOf(categoryCode).imageUrl;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}

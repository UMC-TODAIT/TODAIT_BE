package com.example.TODAIT__BE.domain.place.service.support;

public enum PlaceCategoryDefaultImage {

    CAFE("실제 공개 HTTPS URL"),
    RESTAURANT("실제 공개 HTTPS URL"),
    BAR("실제 공개 HTTPS URL"),
    ACTIVITY("실제 공개 HTTPS URL");

    private final String imageUrl;

    PlaceCategoryDefaultImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public static String getImageUrl(String categoryCode) {
        return valueOf(categoryCode).imageUrl;
    }
}

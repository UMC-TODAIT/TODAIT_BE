package com.example.TODAIT__BE.domain.place.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PlaceCategoryDefaultImageTest {

    @Test
    void defaultImageUrlIsNullUntilPublicHttpsUrlIsConfigured() {
        for (PlaceCategoryDefaultImage defaultImage
                : PlaceCategoryDefaultImage.values()) {
            String imageUrl = defaultImage.getImageUrl();

            assertThat(imageUrl)
                    .satisfiesAnyOf(
                            value -> assertThat(value).isNull(),
                            value -> assertThat(value).startsWith("https://")
                    );
        }
    }

    @Test
    void returnsNullForUnknownCategoryCode() {
        assertThat(PlaceCategoryDefaultImage.getImageUrl("UNKNOWN"))
                .isNull();
    }

    @Test
    void trimsCategoryCodeBeforeLookup() {
        assertThat(PlaceCategoryDefaultImage.getImageUrl(" CAFE "))
                .isEqualTo(PlaceCategoryDefaultImage.CAFE.getImageUrl());
    }

    @Test
    void isSupportedReturnsTrueOnlyForKnownCategoryCodes() {
        assertThat(PlaceCategoryDefaultImage.isSupported("CAFE")).isTrue();
        assertThat(PlaceCategoryDefaultImage.isSupported(" BAR ")).isTrue();
        assertThat(PlaceCategoryDefaultImage.isSupported("DESSERT")).isFalse();
        assertThat(PlaceCategoryDefaultImage.isSupported(null)).isFalse();
        assertThat(PlaceCategoryDefaultImage.isSupported(" ")).isFalse();
    }
}

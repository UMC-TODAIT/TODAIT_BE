package com.example.TODAIT__BE.domain.taxonomy.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PlaceCategoryTest {

    @Test
    void createsWithValidValues() {
        PlaceCategory category = PlaceCategory.of("OTHER", "기타", "설명", 5, true);

        assertThat(category.getCode()).isEqualTo("OTHER");
        assertThat(category.getName()).isEqualTo("기타");
        assertThat(category.getDescription()).isEqualTo("설명");
        assertThat(category.getSortOrder()).isEqualTo(5);
        assertThat(category.getIsActive()).isTrue();
    }

    @Test
    void allowsNullDescription() {
        assertThatCode(() -> PlaceCategory.of("OTHER", "기타", null, 5, true))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsBlankCodeOrName() {
        assertThatThrownBy(() -> PlaceCategory.of(" ", "기타", null, 5, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PlaceCategory.of("OTHER", "", null, 5, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullSortOrderOrIsActive() {
        assertThatThrownBy(() -> PlaceCategory.of("OTHER", "기타", null, null, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PlaceCategory.of("OTHER", "기타", null, 5, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

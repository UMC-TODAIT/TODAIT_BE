package com.example.TODAIT__BE.domain.place.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class PlaceSourceTest {

    @Test
    void sourcePlaceIdIsRequiredByEntityMapping() throws NoSuchFieldException {
        Field sourcePlaceId =
                PlaceSource.class.getDeclaredField("sourcePlaceId");

        Column column = sourcePlaceId.getAnnotation(Column.class);

        assertThat(column).isNotNull();
        assertThat(column.name()).isEqualTo("source_place_id");
        assertThat(column.nullable()).isFalse();
    }
}

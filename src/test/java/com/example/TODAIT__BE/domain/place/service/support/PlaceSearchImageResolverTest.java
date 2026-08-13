package com.example.TODAIT__BE.domain.place.service.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceSearchImageType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlaceSearchImageResolverTest {

    private final PlaceSearchImageResolver resolver =
            new PlaceSearchImageResolver();

    @Test
    void usesPrimaryPlaceImageBeforePlaceDefaultImage() {
        Place place = mock(Place.class);

        given(place.getId()).willReturn(1L);
        given(place.getDefaultImageUrl())
                .willReturn("https://example.com/default.jpg");

        PlaceSearchImageResolver.ImageSelection selection =
                resolver.resolve(
                        place,
                        Map.of(1L, "https://example.com/primary.jpg")
                );

        assertThat(selection.imageUrl())
                .isEqualTo("https://example.com/primary.jpg");
        assertThat(selection.imageType())
                .isEqualTo(PlaceSearchImageType.PLACE_IMAGE);
    }

    @Test
    void returnsNullWhenRegisteredPlaceImageDoesNotExist() {
        PlaceSearchImageResolver.ImageSelection selection =
                resolver.resolve(
                        null,
                        Map.of()
                );

        assertThat(selection.imageUrl()).isNull();
        assertThat(selection.imageType()).isNull();
    }
}

package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceSearchImageType;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PlaceSearchImageResolver {

    public ImageSelection resolve(
            Place registeredPlace,
            PlaceCategory category,
            Map<Long, String> primaryImageUrlsByPlaceId
    ) {
        if (registeredPlace != null) {
            String primaryImageUrl =
                    primaryImageUrlsByPlaceId.get(
                            registeredPlace.getId()
                    );

            if (hasText(primaryImageUrl)) {
                return new ImageSelection(
                        primaryImageUrl,
                        PlaceSearchImageType.PLACE_IMAGE
                );
            }

            if (hasText(registeredPlace.getDefaultImageUrl())) {
                return new ImageSelection(
                        registeredPlace.getDefaultImageUrl().trim(),
                        PlaceSearchImageType.PLACE_IMAGE
                );
            }
        }

        return new ImageSelection(
                PlaceCategoryDefaultImage.getImageUrl(
                        category.getCode()
                ),
                PlaceSearchImageType.CATEGORY_DEFAULT
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record ImageSelection(
            String imageUrl,
            PlaceSearchImageType imageType
    ) {
    }
}

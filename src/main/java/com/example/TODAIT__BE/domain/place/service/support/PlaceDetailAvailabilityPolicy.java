package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PlaceDetailAvailabilityPolicy {

    public boolean isAvailable(
            Place place,
            Set<Long> operatorSourcePlaceIds
    ) {
        if (place == null) {
            return false;
        }

        return operatorSourcePlaceIds.contains(place.getId())
                && Boolean.TRUE.equals(place.getIsActive())
                && place.getReviewStatus() == PlaceReviewStatus.APPROVED
                && place.getExposureStatus() == PlaceExposureStatus.ACTIVE
                && place.getDeletedAt() == null
                && hasRequiredDetailData(place);
    }

    private boolean hasRequiredDetailData(Place place) {
        return hasText(place.getName())
                && hasText(place.getAddress())
                && place.getLatitude() != null
                && place.getLongitude() != null
                && place.getArea() != null
                && place.getPlaceCategory() != null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

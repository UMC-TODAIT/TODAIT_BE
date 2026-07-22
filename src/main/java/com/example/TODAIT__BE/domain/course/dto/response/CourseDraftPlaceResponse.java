package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.place.entity.Place;

public record CourseDraftPlaceResponse(
        Long courseDraftPlaceId,
        Long placeId,
        PlaceRole placeRole,
        Integer visitOrder,
        String name,
        String address,
        Double latitude,
        Double longitude
) {

    public static CourseDraftPlaceResponse from(CourseDraftPlace courseDraftPlace) {
        Place place = courseDraftPlace.getPlace();
        return new CourseDraftPlaceResponse(
                courseDraftPlace.getId(),
                place.getId(),
                courseDraftPlace.getPlaceRole(),
                courseDraftPlace.getVisitOrder(),
                place.getName(),
                place.getAddress(),
                place.getLatitude(),
                place.getLongitude()
        );
    }
}

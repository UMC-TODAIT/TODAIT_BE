package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.place.entity.Place;

public record OrderingEntryPlaceResponse(
        Long courseDraftPlaceId,
        Long placeId,
        Integer visitOrder,
        PlaceRole placeRole,
        boolean draggable,
        boolean deletable,
        String name,
        String address,
        String roadAddress,
        Double latitude,
        Double longitude
) {

    public static OrderingEntryPlaceResponse from(CourseDraftPlace courseDraftPlace) {
        Place place = courseDraftPlace.getPlace();
        boolean selected = courseDraftPlace.getPlaceRole() == PlaceRole.SELECTED;
        return new OrderingEntryPlaceResponse(
                courseDraftPlace.getId(),
                place.getId(),
                courseDraftPlace.getVisitOrder(),
                courseDraftPlace.getPlaceRole(),
                selected,   // BASE 장소는 드래그 불가, SELECTED만 가능
                selected,   // BASE 장소는 삭제 불가, SELECTED만 가능
                place.getName(),
                place.getAddress(),
                place.getRoadAddress(),
                place.getLatitude(),
                place.getLongitude()
        );
    }
}

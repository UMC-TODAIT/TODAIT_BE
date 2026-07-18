package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;

public record CoursePlaceResponse(
        Long coursePlaceId,
        Long placeId,
        PlaceRole placeRole,
        Integer visitOrder,
        String name,
        String address,
        String memo
) {

    public static CoursePlaceResponse from(CoursePlace coursePlace) {
        return new CoursePlaceResponse(
                coursePlace.getId(),
                coursePlace.getPlace().getId(),
                coursePlace.getPlaceRole(),
                coursePlace.getVisitOrder(),
                coursePlace.getPlaceNameSnapshot(),
                coursePlace.getAddressSnapshot(),
                coursePlace.getMemo()
        );
    }
}

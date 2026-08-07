package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;

public record CourseDraftPlaceAddResponse(
        Long courseDraftId,
        CourseDraftStatus draftStatus,
        AddedPlace addedPlace,
        Integer selectedPlaceCount,
        Integer totalPlaceCount
) {

    public static CourseDraftPlaceAddResponse of(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            CourseDraftPlace addedDraftPlace,
            int selectedPlaceCount,
            int totalPlaceCount
    ) {
        return new CourseDraftPlaceAddResponse(
                courseDraftId,
                draftStatus,
                AddedPlace.from(addedDraftPlace),
                selectedPlaceCount,
                totalPlaceCount
        );
    }

    public record AddedPlace(
            Long courseDraftPlaceId,
            Long placeId,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude,
            AreaSummary area,
            CategorySummary category,
            String subCategory,
            String imageUrl,
            Integer visitOrder,
            PlaceRole placeRole
    ) {
        public static AddedPlace from(CourseDraftPlace draftPlace) {
            Place place = draftPlace.getPlace();
            return new AddedPlace(
                    draftPlace.getId(),
                    place.getId(),
                    place.getName(),
                    place.getAddress(),
                    place.getRoadAddress(),
                    place.getLatitude(),
                    place.getLongitude(),
                    AreaSummary.from(place.getArea()),
                    CategorySummary.from(place.getPlaceCategory()),
                    place.getSubCategory(),
                    place.getDefaultImageUrl(),
                    draftPlace.getVisitOrder(),
                    draftPlace.getPlaceRole()
            );
        }
    }

    public record AreaSummary(
            Long areaId,
            String code,
            String name
    ) {
        public static AreaSummary from(Area area) {
            return new AreaSummary(area.getId(), area.getCode(), area.getName());
        }
    }

    public record CategorySummary(
            Long placeCategoryId,
            String code,
            String name
    ) {
        public static CategorySummary from(PlaceCategory placeCategory) {
            return new CategorySummary(placeCategory.getId(), placeCategory.getCode(), placeCategory.getName());
        }
    }
}

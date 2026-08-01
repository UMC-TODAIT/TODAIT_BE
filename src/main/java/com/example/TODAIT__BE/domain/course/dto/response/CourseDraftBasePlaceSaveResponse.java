package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;

public record CourseDraftBasePlaceSaveResponse(
        Long courseDraftId,
        BasePlace basePlace,
        CourseDraftStatus draftStatus
) {

    public static CourseDraftBasePlaceSaveResponse of(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            BasePlace basePlace
    ) {
        return new CourseDraftBasePlaceSaveResponse(courseDraftId, basePlace, draftStatus);
    }

    public record BasePlace(
            Long placeId,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude,
            AreaSummary area,
            CategorySummary category,
            String subCategory,
            String sourceType,
            Boolean isNewPlace,
            Integer visitOrder,
            PlaceRole placeRole
    ) {
        public static BasePlace of(CourseDraftPlace baseDraftPlace, String sourceType, boolean isNewPlace) {
            Place place = baseDraftPlace.getPlace();
            return new BasePlace(
                    place.getId(),
                    place.getName(),
                    place.getAddress(),
                    place.getRoadAddress(),
                    place.getLatitude(),
                    place.getLongitude(),
                    AreaSummary.from(place.getArea()),
                    CategorySummary.from(place.getPlaceCategory()),
                    place.getSubCategory(),
                    sourceType,
                    isNewPlace,
                    baseDraftPlace.getVisitOrder(),
                    baseDraftPlace.getPlaceRole()
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

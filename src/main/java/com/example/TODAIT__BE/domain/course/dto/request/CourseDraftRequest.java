package com.example.TODAIT__BE.domain.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class CourseDraftRequest {

    private CourseDraftRequest() {
    }

    public record MoodTagSaveRequest(
            List<Long> moodTagIds
    ) {
    }

    public record FoodCategorySaveRequest(
            List<Long> foodCategoryIds
    ) {
    }

    public record BasePlaceSaveRequest(
            Long placeId,
            ExternalPlace externalPlace
    ) {

        public record ExternalPlace(
                String dataSourceCode,
                String sourcePlaceId,
                String name,
                String address,
                String roadAddress,
                Double latitude,
                Double longitude,
                String areaCode,
                String categoryCode,
                String subCategory,
                String phone,
                String sourceUrl
        ) {
        }
    }

    public record PlaceAddRequest(
            @NotNull
            Long placeId
    ) {
    }

    public record PlaceOrderUpdateRequest(
            @Valid
            @NotNull
            List<PlaceOrderItem> placeOrders
    ) {

        public record PlaceOrderItem(
                @NotNull
                Long courseDraftPlaceId,
                @NotNull
                Integer visitOrder
        ) {
        }
    }
}

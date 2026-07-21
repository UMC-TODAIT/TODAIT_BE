package com.example.TODAIT__BE.domain.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

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

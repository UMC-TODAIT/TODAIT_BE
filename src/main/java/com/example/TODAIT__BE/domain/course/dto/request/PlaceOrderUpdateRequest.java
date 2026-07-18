package com.example.TODAIT__BE.domain.course.dto.request;

import java.util.List;

public record PlaceOrderUpdateRequest(
        List<PlaceOrderItem> placeOrders
) {

    public record PlaceOrderItem(
            Long courseDraftPlaceId,
            Integer visitOrder
    ) {
    }
}

package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import java.util.List;

public record OrderingEntryResponse(
        Long courseDraftId,
        CourseDraftStatus draftStatus,
        int totalPlaceCount,
        int selectedPlaceCount,
        List<OrderingEntryPlaceResponse> places
) {
}

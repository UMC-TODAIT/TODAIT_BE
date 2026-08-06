package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import java.util.List;

public record CourseDraftSavingEnterResponse(
        Long courseDraftId,
        CourseDraftStatus draftStatus,
        int totalPlaceCount,
        List<CourseDraftPlaceResponse> routePreview
) {

    public static CourseDraftSavingEnterResponse of(
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            List<CourseDraftPlaceResponse> routePreview
    ) {
        return new CourseDraftSavingEnterResponse(
                courseDraftId,
                draftStatus,
                routePreview.size(),
                routePreview
        );
    }
}

package com.example.TODAIT__BE.domain.course.dto.request;

import java.util.List;

public final class CourseSaveRequest {

    private CourseSaveRequest() {
    }

    public record SaveRequest(
            String title,
            String memo,
            List<Long> moodTagIds
    ) {
    }
}

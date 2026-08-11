package com.example.TODAIT__BE.domain.course.dto.request;

public final class CourseSaveRequest {

    private CourseSaveRequest() {
    }

    public record SaveRequest(
            String title,
            String memo
    ) {
    }
}

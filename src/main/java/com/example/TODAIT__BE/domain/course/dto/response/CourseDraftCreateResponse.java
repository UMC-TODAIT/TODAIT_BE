package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import java.time.format.DateTimeFormatter;

public record CourseDraftCreateResponse(
        Long courseDraftId,
        CourseDraftStatus status,
        String expiresAt,
        String createdAt
) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static CourseDraftCreateResponse from(CourseDraft courseDraft) {
        return new CourseDraftCreateResponse(
                courseDraft.getId(),
                courseDraft.getStatus(),
                FORMATTER.format(courseDraft.getExpiresAt()),
                FORMATTER.format(courseDraft.getCreatedAt())
        );
    }
}

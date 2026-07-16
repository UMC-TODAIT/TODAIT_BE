package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import java.time.LocalDateTime;

public record CourseDraftCreateResponse(
        Long courseDraftId,
        CourseDraftStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {

    public static CourseDraftCreateResponse from(CourseDraft courseDraft) {
        return new CourseDraftCreateResponse(
                courseDraft.getId(),
                courseDraft.getStatus(),
                courseDraft.getExpiresAt(),
                courseDraft.getCreatedAt()
        );
    }
}

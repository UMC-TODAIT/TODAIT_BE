package com.example.TODAIT__BE.domain.course.service.validator;

import com.example.TODAIT__BE.domain.course.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class CourseDraftValidator {

    public void validateOwner(CourseDraft courseDraft, Long memberId) {
        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED);
        }
    }

    public void validateStatus(
            CourseDraft courseDraft,
            CourseDraftStatus expectedStatus,
            CourseErrorCode errorCode
    ) {
        if (courseDraft.getStatus() != expectedStatus) {
            throw new CourseException(errorCode);
        }
    }

    public void validateStatusIn(
            CourseDraft courseDraft,
            CourseErrorCode errorCode,
            CourseDraftStatus... allowedStatuses
    ) {
        boolean allowed = Arrays.stream(allowedStatuses)
                .anyMatch(status -> courseDraft.getStatus() == status);
        if (!allowed) {
            throw new CourseException(errorCode);
        }
    }
}

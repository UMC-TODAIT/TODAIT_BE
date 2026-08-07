package com.example.TODAIT__BE.domain.course.dto.request;

import java.util.List;

public record CourseSaveRequest(
        String title,
        String memo,
        List<Long> moodTagIds
) {
}

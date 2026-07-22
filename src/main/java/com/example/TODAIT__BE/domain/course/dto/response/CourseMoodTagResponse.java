package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;

public record CourseMoodTagResponse(
        Long moodTagId,
        String code,
        String name
) {

    public static CourseMoodTagResponse from(MoodTag moodTag) {
        return new CourseMoodTagResponse(moodTag.getId(), moodTag.getCode(), moodTag.getName());
    }
}

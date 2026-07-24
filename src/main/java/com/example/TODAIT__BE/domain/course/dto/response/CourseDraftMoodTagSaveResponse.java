package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import java.util.List;

public record CourseDraftMoodTagSaveResponse(
        Long courseDraftId,
        CourseDraftStatus status,
        List<MoodTagItem> moodTags
) {

    public static CourseDraftMoodTagSaveResponse of(
            Long courseDraftId,
            CourseDraftStatus status,
            List<MoodTag> moodTags
    ) {
        return new CourseDraftMoodTagSaveResponse(
                courseDraftId,
                status,
                moodTags.stream().map(MoodTagItem::from).toList()
        );
    }

    public record MoodTagItem(
            Long moodTagId,
            String code,
            String name
    ) {
        public static MoodTagItem from(MoodTag moodTag) {
            return new MoodTagItem(moodTag.getId(), moodTag.getCode(), moodTag.getName());
        }
    }
}

package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftMoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftMoodTagSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftMoodTag;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomyErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseDraftMoodTagService {

    private static final int MIN_MOOD_TAG_COUNT = 2;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    private final MoodTagRepository moodTagRepository;

    @Transactional
    public CourseDraftMoodTagSaveResponse saveMoodTags(
            Long courseDraftId,
            Long memberId,
            CourseDraftMoodTagSaveRequest request
    ) {
        CourseDraft courseDraft = courseDraftRepository.findById(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED);
        }

        List<Long> moodTagIds = request.moodTagIds();

        if (moodTagIds == null || moodTagIds.size() < MIN_MOOD_TAG_COUNT) {
            throw new CourseException(CourseErrorCode.INVALID_MOOD_TAG_COUNT);
        }

        if (new HashSet<>(moodTagIds).size() != moodTagIds.size()) {
            throw new CourseException(CourseErrorCode.DUPLICATE_MOOD_TAG);
        }

        List<MoodTag> moodTags = validateAndGetMoodTags(moodTagIds);

        courseDraftMoodTagRepository.deleteByCourseDraft(courseDraft);

        for (MoodTag moodTag : moodTags) {
            courseDraftMoodTagRepository.save(
                    CourseDraftMoodTag.builder()
                            .courseDraft(courseDraft)
                            .moodTag(moodTag)
                            .build()
            );
        }

        courseDraft.changeStatus(CourseDraftStatus.FOOD_SELECTING);

        return CourseDraftMoodTagSaveResponse.of(
                courseDraft.getId(),
                courseDraft.getStatus(),
                moodTags
        );
    }

    private List<MoodTag> validateAndGetMoodTags(List<Long> moodTagIds) {
        List<MoodTag> foundMoodTags = moodTagRepository.findAllById(moodTagIds);
        if (foundMoodTags.size() != moodTagIds.size()) {
            throw new TaxonomyException(TaxonomyErrorCode.MOOD_TAG_NOT_FOUND);
        }

        Map<Long, MoodTag> moodTagsById = foundMoodTags.stream()
                .collect(Collectors.toMap(MoodTag::getId, Function.identity()));
        return moodTagIds.stream()
                .map(moodTagsById::get)
                .toList();
    }
}

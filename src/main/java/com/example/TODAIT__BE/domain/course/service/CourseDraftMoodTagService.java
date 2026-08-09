package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.MoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.MoodTagSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftMoodTag;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.taxonomy.code.MoodTagErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseDraftMoodTagService {

    private static final int MIN_MOOD_TAG_COUNT = 2;
    private static final int MAX_MOOD_TAG_COUNT = 6;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    private final MoodTagRepository moodTagRepository;
    private final CourseDraftValidator courseDraftValidator;

    @Transactional
    public MoodTagSaveResponse saveMoodTags(
            Long courseDraftId,
            Long memberId,
            MoodTagSaveRequest request
    ) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND));

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatusIn(
                courseDraft,
                CourseDraftErrorCode.MOOD_TAG_DRAFT_STATUS_CONFLICT,
                CourseDraftStatus.MOOD_SELECTING,
                CourseDraftStatus.FOOD_SELECTING
        );

        List<Long> moodTagIds = request.moodTagIds();

        if (moodTagIds == null
                || moodTagIds.size() < MIN_MOOD_TAG_COUNT
                || moodTagIds.size() > MAX_MOOD_TAG_COUNT) {
            throw new CourseException(CourseDraftErrorCode.INVALID_MOOD_TAG_COUNT);
        }

        if (new HashSet<>(moodTagIds).size() != moodTagIds.size()) {
            throw new CourseException(CourseDraftErrorCode.DUPLICATE_MOOD_TAG);
        }

        List<MoodTag> moodTags = validateAndGetMoodTags(moodTagIds);

        updateMoodTags(courseDraft, moodTags);

        if (courseDraft.getStatus() == CourseDraftStatus.MOOD_SELECTING) {
            courseDraft.changeStatus(CourseDraftStatus.FOOD_SELECTING);
        }

        return MoodTagSaveResponse.of(
                courseDraft.getId(),
                courseDraft.getStatus(),
                moodTags
        );
    }

    private List<MoodTag> validateAndGetMoodTags(List<Long> moodTagIds) {
        List<MoodTag> foundMoodTags = moodTagRepository.findAllById(moodTagIds);
        if (foundMoodTags.size() != moodTagIds.size()) {
            throw new TaxonomyException(MoodTagErrorCode.MOOD_TAG_NOT_FOUND);
        }

        Map<Long, MoodTag> moodTagsById = foundMoodTags.stream()
                .collect(Collectors.toMap(MoodTag::getId, Function.identity()));
        return moodTagIds.stream()
                .map(moodTagsById::get)
                .toList();
    }

    private void updateMoodTags(CourseDraft courseDraft, List<MoodTag> moodTags) {
        List<CourseDraftMoodTag> existingMoodTags = courseDraftMoodTagRepository.findByCourseDraft(courseDraft);
        Set<Long> requestedMoodTagIds = moodTags.stream()
                .map(MoodTag::getId)
                .collect(Collectors.toSet());
        Set<Long> existingMoodTagIds = existingMoodTags.stream()
                .map(courseDraftMoodTag -> courseDraftMoodTag.getMoodTag().getId())
                .collect(Collectors.toSet());

        List<CourseDraftMoodTag> moodTagsToDelete = existingMoodTags.stream()
                .filter(courseDraftMoodTag -> !requestedMoodTagIds.contains(courseDraftMoodTag.getMoodTag().getId()))
                .toList();
        courseDraftMoodTagRepository.deleteAll(moodTagsToDelete);

        moodTags.stream()
                .filter(moodTag -> !existingMoodTagIds.contains(moodTag.getId()))
                .map(moodTag -> CourseDraftMoodTag.builder()
                        .courseDraft(courseDraft)
                        .moodTag(moodTag)
                        .build())
                .forEach(courseDraftMoodTagRepository::save);
    }
}

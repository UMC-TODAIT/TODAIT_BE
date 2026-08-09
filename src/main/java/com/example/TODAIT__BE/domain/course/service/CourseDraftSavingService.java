package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftPlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftSavingEnterResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseDraftSavingService {

    private static final int BASE_VISIT_ORDER = 1;
    private static final int SELECTED_PLACE_START_ORDER = 2;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;
    private final CourseDraftValidator courseDraftValidator;

    @Transactional
    public CourseDraftSavingEnterResponse enterSaving(Long courseDraftId, Long memberId) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND));

        courseDraftValidator.validateOwner(courseDraft, memberId);
        courseDraftValidator.validateStatusIn(
                courseDraft,
                CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT,
                CourseDraftStatus.ORDERING,
                CourseDraftStatus.SAVING
        );

        List<CourseDraftPlace> draftPlaces =
                courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(courseDraft);
        validatePlaces(draftPlaces);

        if (courseDraft.getStatus() == CourseDraftStatus.ORDERING) {
            courseDraft.changeStatus(CourseDraftStatus.SAVING);
        }

        List<CourseDraftPlaceResponse> routePreview = draftPlaces.stream()
                .map(CourseDraftPlaceResponse::from)
                .toList();

        return CourseDraftSavingEnterResponse.of(
                courseDraft.getId(),
                courseDraft.getStatus(),
                routePreview
        );
    }

    private void validatePlaces(List<CourseDraftPlace> draftPlaces) {
        List<CourseDraftPlace> basePlaces = draftPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.BASE)
                .toList();

        if (basePlaces.size() != 1
                || basePlaces.get(0).getPlace() == null
                || !Integer.valueOf(BASE_VISIT_ORDER).equals(basePlaces.get(0).getVisitOrder())) {
            throw new CourseException(CourseDraftErrorCode.COURSE_DRAFT_BASE_PLACE_CONFLICT);
        }

        List<CourseDraftPlace> selectedPlaces = draftPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.SELECTED)
                .sorted(Comparator.comparing(
                        CourseDraftPlace::getVisitOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .toList();

        if (selectedPlaces.isEmpty()) {
            throw new CourseException(CourseDraftErrorCode.COURSE_DRAFT_SELECTED_PLACE_CONFLICT);
        }

        for (int i = 0; i < selectedPlaces.size(); i++) {
            int expectedOrder = SELECTED_PLACE_START_ORDER + i;
            if (selectedPlaces.get(i).getPlace() == null
                    || !Integer.valueOf(expectedOrder).equals(selectedPlaces.get(i).getVisitOrder())) {
                throw new CourseException(CourseDraftErrorCode.COURSE_DRAFT_SELECTED_PLACE_CONFLICT);
            }
        }
    }
}

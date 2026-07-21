package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest.PlaceOrderItem;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftPlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.PlaceOrderUpdateResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
public class CourseDraftPlaceService {

    private static final int SELECTED_PLACE_START_ORDER = 2;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;

    @Transactional
    public PlaceOrderUpdateResponse updatePlaceOrder(Long courseDraftId, Long memberId, PlaceOrderUpdateRequest request) {
        CourseDraft courseDraft = courseDraftRepository.findById(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(CourseErrorCode.NOT_COURSE_DRAFT_OWNER);
        }
        validateEditableDraft(courseDraft);

        List<PlaceOrderItem> placeOrders = request.placeOrders() != null ? request.placeOrders() : List.of();

        List<CourseDraftPlace> allPlaces =
                courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(courseDraft);

        List<CourseDraftPlace> targetPlaces = resolveTargetPlaces(allPlaces, placeOrders);
        validateVisitOrders(allPlaces, placeOrders);

        updateVisitOrders(targetPlaces, placeOrders);

        List<CourseDraftPlaceResponse> responses = allPlaces.stream()
                .sorted(Comparator.comparing(CourseDraftPlace::getVisitOrder))
                .map(CourseDraftPlaceResponse::from)
                .toList();

        return PlaceOrderUpdateResponse.of(courseDraft.getId(), responses);
    }

    private void validateEditableDraft(CourseDraft courseDraft) {
        if (courseDraft.getStatus() == CourseDraftStatus.COMPLETED
                || courseDraft.getStatus() == CourseDraftStatus.ABANDONED
                || courseDraft.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CourseException(CourseErrorCode.INVALID_COURSE_DRAFT_STATUS);
        }
    }

    private List<CourseDraftPlace> resolveTargetPlaces(
            List<CourseDraftPlace> allPlaces,
            List<PlaceOrderItem> placeOrders
    ) {
        Map<Long, CourseDraftPlace> placesById = allPlaces.stream()
                .collect(Collectors.toMap(CourseDraftPlace::getId, Function.identity()));

        List<CourseDraftPlace> targetPlaces = new ArrayList<>();
        for (PlaceOrderItem item : placeOrders) {
            CourseDraftPlace place = placesById.get(item.courseDraftPlaceId());
            if (place == null) {
                throw new CourseException(CourseErrorCode.SELECTED_PLACE_NOT_FOUND);
            }
            if (place.getPlaceRole() == PlaceRole.BASE) {
                throw new CourseException(CourseErrorCode.BASE_PLACE_NOT_REORDERABLE);
            }
            targetPlaces.add(place);
        }
        return targetPlaces;
    }

    private void updateVisitOrders(List<CourseDraftPlace> targetPlaces, List<PlaceOrderItem> placeOrders) {
        for (int i = 0; i < targetPlaces.size(); i++) {
            targetPlaces.get(i).updateVisitOrder(-(i + 1));
        }
        courseDraftPlaceRepository.flush();

        for (int i = 0; i < targetPlaces.size(); i++) {
            targetPlaces.get(i).updateVisitOrder(placeOrders.get(i).visitOrder());
        }
    }

    private void validateVisitOrders(List<CourseDraftPlace> allPlaces, List<PlaceOrderItem> placeOrders) {
        List<Integer> sortedOrders = placeOrders.stream()
                .map(PlaceOrderItem::visitOrder)
                .sorted()
                .toList();
        for (int i = 0; i < sortedOrders.size(); i++) {
            if (!sortedOrders.get(i).equals(SELECTED_PLACE_START_ORDER + i)) {
                throw new CourseException(CourseErrorCode.INVALID_VISIT_ORDER);
            }
        }

        Set<Long> requestedIds = placeOrders.stream()
                .map(PlaceOrderItem::courseDraftPlaceId)
                .collect(Collectors.toSet());
        Set<Long> actualSelectedIds = allPlaces.stream()
                .filter(place -> place.getPlaceRole() == PlaceRole.SELECTED)
                .map(CourseDraftPlace::getId)
                .collect(Collectors.toSet());

        if (requestedIds.size() != placeOrders.size() || !requestedIds.equals(actualSelectedIds)) {
            throw new CourseException(CourseErrorCode.INVALID_VISIT_ORDER);
        }
    }
}

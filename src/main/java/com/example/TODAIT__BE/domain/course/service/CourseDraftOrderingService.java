package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.OrderingEntryPlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.OrderingEntryResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장소 선택 단계에서 드래그 순서 설정 화면으로 진입할 때 호출되는 API.
 * 장소 구성 무결성을 검증한 뒤 course_draft.status 를 ORDERING 으로 전환한다.
 * 이미 ORDERING 인 경우 상태 변경 없이 재검증 후 동일한 성공 응답을 반환한다(멱등).
 */
@Service
@RequiredArgsConstructor
public class CourseDraftOrderingService {

    private static final int BASE_PLACE_VISIT_ORDER = 1;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;

    @Transactional
    public OrderingEntryResponse enterOrdering(Long courseDraftId, Long memberId) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() ->
                        new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED);
        }

        validateEnterableStatus(courseDraft.getStatus());

        List<CourseDraftPlace> places = courseDraftPlaceRepository
                .findByCourseDraftIdWithPlaceOrderByVisitOrderAsc(courseDraftId);

        int selectedPlaceCount = validatePlaceComposition(places);

        // PLACE_SELECTING 최초 진입 시에만 상태를 전환하고, ORDERING 재진입 시엔 유지한다.
        if (courseDraft.getStatus() == CourseDraftStatus.PLACE_SELECTING) {
            courseDraft.changeStatus(CourseDraftStatus.ORDERING);
        }

        return new OrderingEntryResponse(
                courseDraft.getId(),
                courseDraft.getStatus(),
                places.size(),
                selectedPlaceCount,
                places.stream()
                        .map(OrderingEntryPlaceResponse::from)
                        .toList()
        );
    }

    private void validateEnterableStatus(CourseDraftStatus status) {
        if (status != CourseDraftStatus.PLACE_SELECTING
                && status != CourseDraftStatus.ORDERING) {
            throw new CourseException(
                    CourseErrorCode.ORDERING_ENTRY_STATUS_CONFLICT);
        }
    }

    /**
     * 장소 구성 무결성을 검증하고 SELECTED 장소 개수를 반환한다.
     * - BASE 장소는 정확히 1개이며 visit_order 는 1이어야 한다.
     * - SELECTED 장소는 최소 1개 이상이어야 한다.
     * - visit_order 는 1부터 중복 없이 연속되어야 한다.
     */
    private int validatePlaceComposition(List<CourseDraftPlace> places) {
        List<CourseDraftPlace> basePlaces = places.stream()
                .filter(place -> place.getPlaceRole() == PlaceRole.BASE)
                .toList();
        if (basePlaces.size() != 1
                || basePlaces.get(0).getVisitOrder() != BASE_PLACE_VISIT_ORDER) {
            throw new CourseException(
                    CourseErrorCode.ORDERING_ENTRY_INVALID_BASE_PLACE);
        }

        long selectedPlaceCount = places.stream()
                .filter(place -> place.getPlaceRole() == PlaceRole.SELECTED)
                .count();
        if (selectedPlaceCount == 0) {
            throw new CourseException(
                    CourseErrorCode.ORDERING_ENTRY_SELECTED_PLACE_REQUIRED);
        }

        validateContiguousVisitOrders(places);

        return (int) selectedPlaceCount;
    }

    private void validateContiguousVisitOrders(List<CourseDraftPlace> places) {
        // places 는 visit_order ASC 로 조회됨. 1..N 으로 중복 없이 연속이어야 한다.
        for (int i = 0; i < places.size(); i++) {
            Integer visitOrder = places.get(i).getVisitOrder();
            if (visitOrder == null || visitOrder != i + 1) {
                throw new CourseException(
                        CourseErrorCode.ORDERING_ENTRY_INVALID_VISIT_ORDER);
            }
        }
    }
}

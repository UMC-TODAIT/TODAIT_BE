package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftPlaceAddRequest;
import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest.PlaceOrderItem;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftPlaceAddResponse;
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
import com.example.TODAIT__BE.domain.place.code.ExternalPlaceRegistrationErrorCode;
import com.example.TODAIT__BE.domain.place.code.PlaceDetailErrorCode;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.place.service.support.PlaceCategoryDefaultImage;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
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

    private static final int BASE_VISIT_ORDER = 1;
    private static final int SELECTED_PLACE_START_ORDER = 2;

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public PlaceOrderUpdateResponse updatePlaceOrder(Long courseDraftId, Long memberId, PlaceOrderUpdateRequest request) {
        CourseDraft courseDraft = courseDraftRepository.findById(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED);
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

    @Transactional
    public CourseDraftPlaceAddResponse addPlace(Long courseDraftId, Long memberId, CourseDraftPlaceAddRequest request) {
        CourseDraft courseDraft = courseDraftRepository.findByIdForUpdate(courseDraftId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        if (!courseDraft.getMember().getId().equals(memberId)) {
            throw new CourseException(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED);
        }
        if (courseDraft.getStatus() != CourseDraftStatus.PLACE_SELECTING) {
            throw new CourseException(CourseErrorCode.PLACE_ADD_DRAFT_STATUS_CONFLICT);
        }

        List<CourseDraftPlace> existingPlaces =
                courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(courseDraft);
        CourseDraftPlace basePlace = validateBasePlaceIntegrity(existingPlaces);

        Place place = placeRepository.findById(request.placeId())
                .orElseThrow(() -> new PlaceException(PlaceDetailErrorCode.PLACE_NOT_FOUND));
        validateAvailablePlace(place);

        if (place.getId().equals(basePlace.getPlace().getId())) {
            throw new CourseException(CourseErrorCode.BASE_PLACE_RESELECT_CONFLICT);
        }

        boolean alreadySelected = existingPlaces.stream()
                .anyMatch(draftPlace -> draftPlace.getPlace().getId().equals(place.getId()));
        if (alreadySelected) {
            throw new CourseException(CourseErrorCode.SELECTED_PLACE_DUPLICATE);
        }

        boolean categoryAlreadyUsed = existingPlaces.stream()
                .anyMatch(draftPlace ->
                        draftPlace.getPlace().getPlaceCategory().getId().equals(place.getPlaceCategory().getId()));
        if (categoryAlreadyUsed) {
            throw new CourseException(CourseErrorCode.SELECTED_PLACE_CATEGORY_DUPLICATE);
        }

        int nextVisitOrder = existingPlaces.stream()
                .mapToInt(CourseDraftPlace::getVisitOrder)
                .max()
                .orElse(BASE_VISIT_ORDER) + 1;

        CourseDraftPlace savedPlace = courseDraftPlaceRepository.save(CourseDraftPlace.builder()
                .courseDraft(courseDraft)
                .place(place)
                .visitOrder(nextVisitOrder)
                .placeRole(PlaceRole.SELECTED)
                .build());

        int selectedPlaceCount = (int) existingPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.SELECTED)
                .count() + 1;
        int totalPlaceCount = existingPlaces.size() + 1;

        return CourseDraftPlaceAddResponse.of(
                courseDraft.getId(), courseDraft.getStatus(), savedPlace, selectedPlaceCount, totalPlaceCount
        );
    }

    private CourseDraftPlace validateBasePlaceIntegrity(List<CourseDraftPlace> existingPlaces) {
        List<CourseDraftPlace> baseDraftPlaces = existingPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.BASE)
                .toList();
        if (baseDraftPlaces.size() != 1 || !baseDraftPlaces.get(0).getVisitOrder().equals(BASE_VISIT_ORDER)) {
            throw new CourseException(CourseErrorCode.INVALID_BASE_PLACE);
        }
        return baseDraftPlaces.get(0);
    }

    private void validateAvailablePlace(Place place) {
        Area area = place.getArea();
        PlaceCategory placeCategory = place.getPlaceCategory();

        boolean available = Boolean.TRUE.equals(place.getIsActive())
                && place.getReviewStatus() == PlaceReviewStatus.APPROVED
                && place.getExposureStatus() == PlaceExposureStatus.ACTIVE
                && place.getDeletedAt() == null
                && area != null && Boolean.TRUE.equals(area.getIsActive())
                && placeCategory != null && Boolean.TRUE.equals(placeCategory.getIsActive())
                && PlaceCategoryDefaultImage.isSupported(placeCategory.getCode())
                && place.getLatitude() != null
                && place.getLongitude() != null;

        if (!available) {
            throw new PlaceException(ExternalPlaceRegistrationErrorCode.PLACE_NOT_AVAILABLE);
        }
    }

    private void validateEditableDraft(CourseDraft courseDraft) {
        if (courseDraft.getStatus() != CourseDraftStatus.ORDERING) {
            throw new CourseException(CourseErrorCode.PLACE_ORDER_DRAFT_STATUS_CONFLICT);
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

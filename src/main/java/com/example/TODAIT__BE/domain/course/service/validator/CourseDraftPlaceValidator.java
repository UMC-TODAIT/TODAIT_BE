package com.example.TODAIT__BE.domain.course.service.validator;

import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import java.util.Comparator;
import java.util.List;

public final class CourseDraftPlaceValidator {

    private static final int BASE_VISIT_ORDER = 1;
    private static final int SELECTED_PLACE_START_ORDER = 2;

    private CourseDraftPlaceValidator() {
    }

    public static CourseDraftPlace validateBasePlaceIntegrity(List<CourseDraftPlace> existingPlaces) {
        List<CourseDraftPlace> baseDraftPlaces = existingPlaces.stream()
                .filter(draftPlace -> draftPlace.getPlaceRole() == PlaceRole.BASE)
                .toList();
        if (baseDraftPlaces.size() != 1
                || !Integer.valueOf(BASE_VISIT_ORDER).equals(baseDraftPlaces.get(0).getVisitOrder())) {
            throw new CourseException(CourseDraftErrorCode.INVALID_BASE_PLACE);
        }
        return baseDraftPlaces.get(0);
    }

    public static int validateOrderingPlaceComposition(List<CourseDraftPlace> places) {
        List<CourseDraftPlace> basePlaces = places.stream()
                .filter(place -> place.getPlaceRole() == PlaceRole.BASE)
                .toList();
        if (basePlaces.size() != 1
                || !Integer.valueOf(BASE_VISIT_ORDER).equals(basePlaces.get(0).getVisitOrder())) {
            throw new CourseException(
                    CourseDraftErrorCode.ORDERING_ENTRY_INVALID_BASE_PLACE);
        }

        long selectedPlaceCount = places.stream()
                .filter(place -> place.getPlaceRole() == PlaceRole.SELECTED)
                .count();
        if (selectedPlaceCount == 0) {
            throw new CourseException(
                    CourseDraftErrorCode.ORDERING_ENTRY_SELECTED_PLACE_REQUIRED);
        }

        validateContiguousVisitOrders(places);

        return (int) selectedPlaceCount;
    }

    public static void validateSavingPlaces(List<CourseDraftPlace> draftPlaces) {
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

    private static void validateContiguousVisitOrders(List<CourseDraftPlace> places) {
        for (int i = 0; i < places.size(); i++) {
            Integer visitOrder = places.get(i).getVisitOrder();
            if (visitOrder == null || !visitOrder.equals(i + 1)) {
                throw new CourseException(
                        CourseDraftErrorCode.ORDERING_ENTRY_INVALID_VISIT_ORDER);
            }
        }
    }
}

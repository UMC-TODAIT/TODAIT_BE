package com.example.TODAIT__BE.domain.course.enums;

public enum CourseDraftStatus {
    MOOD_SELECTING(1),
    FOOD_SELECTING(2),
    BASE_PLACE_SELECTING(3),
    PLACE_SELECTING(4),
    ORDERING(5),
    SAVING(6),
    COMPLETED(0),
    ABANDONED(0);

    private final int workflowStep;

    CourseDraftStatus(int workflowStep) {
        this.workflowStep = workflowStep;
    }

    public boolean canMoveBackTo(CourseDraftStatus targetStatus) {
        return isProgressStatus()
                && targetStatus != null
                && targetStatus.isProgressStatus()
                && targetStatus.workflowStep < workflowStep;
    }

    private boolean isProgressStatus() {
        return workflowStep > 0;
    }
}

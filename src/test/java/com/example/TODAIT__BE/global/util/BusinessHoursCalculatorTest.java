package com.example.TODAIT__BE.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.place.enums.BusinessStatus;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class BusinessHoursCalculatorTest {

    @Test
    void returnsOpenWhenNowIsWithinDaytimeHours() {
        assertThat(BusinessHoursCalculator.calculate("11:00-21:00", LocalTime.of(15, 0)))
                .isEqualTo(BusinessStatus.OPEN);
    }

    @Test
    void returnsOpenAtOpeningBoundaryAndClosedAtClosingBoundary() {
        assertThat(BusinessHoursCalculator.calculate("11:00-21:00", LocalTime.of(11, 0)))
                .isEqualTo(BusinessStatus.OPEN);
        assertThat(BusinessHoursCalculator.calculate("11:00-21:00", LocalTime.of(21, 0)))
                .isEqualTo(BusinessStatus.CLOSED);
    }

    @Test
    void returnsClosedWhenNowIsOutsideDaytimeHours() {
        assertThat(BusinessHoursCalculator.calculate("11:00-21:00", LocalTime.of(22, 0)))
                .isEqualTo(BusinessStatus.CLOSED);
    }

    @Test
    void handlesOvernightHours() {
        assertThat(BusinessHoursCalculator.calculate("18:00-02:00", LocalTime.of(23, 0)))
                .isEqualTo(BusinessStatus.OPEN);
        assertThat(BusinessHoursCalculator.calculate("18:00-02:00", LocalTime.of(1, 0)))
                .isEqualTo(BusinessStatus.OPEN);
        assertThat(BusinessHoursCalculator.calculate("18:00-02:00", LocalTime.of(3, 0)))
                .isEqualTo(BusinessStatus.CLOSED);
    }

    @Test
    void treatsEqualOpenAndCloseAsAlwaysOpen() {
        assertThat(BusinessHoursCalculator.calculate("00:00-00:00", LocalTime.of(4, 0)))
                .isEqualTo(BusinessStatus.OPEN);
    }

    @Test
    void returnsNullWhenBusinessHoursIsMissingOrUnparseable() {
        assertThat(BusinessHoursCalculator.calculate(null, LocalTime.NOON)).isNull();
        assertThat(BusinessHoursCalculator.calculate("", LocalTime.NOON)).isNull();
        assertThat(BusinessHoursCalculator.calculate("영업중", LocalTime.NOON)).isNull();
        assertThat(BusinessHoursCalculator.calculate("11:00", LocalTime.NOON)).isNull();
        assertThat(BusinessHoursCalculator.calculate("25:00-99:00", LocalTime.NOON)).isNull();
    }
}

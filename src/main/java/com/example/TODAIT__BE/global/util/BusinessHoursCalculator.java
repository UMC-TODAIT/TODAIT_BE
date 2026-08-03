package com.example.TODAIT__BE.global.util;

import com.example.TODAIT__BE.domain.place.enums.BusinessStatus;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 장소 영업시간 원본 데이터(business_hours)를 현재 시각과 비교해 영업 상태를 계산한다.
 *
 * <p>MVP에서는 영업시간을 {@code "HH:mm-HH:mm"} (오픈-마감) 형식으로 저장한다고 가정한다.
 * 마감 시각이 오픈 시각보다 이르면 자정을 넘기는 영업(예: {@code 18:00-02:00})으로 해석한다.
 * 형식을 해석할 수 없거나 값이 없으면 {@code null}(알 수 없음)을 반환한다.
 */
public final class BusinessHoursCalculator {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private BusinessHoursCalculator() {
    }

    public static BusinessStatus calculate(String businessHours, LocalTime now) {
        if (businessHours == null || businessHours.isBlank()) {
            return null;
        }

        String[] parts = businessHours.trim().split("-");
        if (parts.length != 2) {
            return null;
        }

        LocalTime open;
        LocalTime close;
        try {
            open = LocalTime.parse(parts[0].trim(), TIME_FORMAT);
            close = LocalTime.parse(parts[1].trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }

        boolean isOpen;
        if (open.equals(close)) {
            // 오픈과 마감이 같으면 24시간 영업으로 본다.
            isOpen = true;
        } else if (close.isAfter(open)) {
            isOpen = !now.isBefore(open) && now.isBefore(close);
        } else {
            // 자정을 넘기는 영업 (예: 18:00-02:00)
            isOpen = !now.isBefore(open) || now.isBefore(close);
        }

        return isOpen ? BusinessStatus.OPEN : BusinessStatus.CLOSED;
    }
}

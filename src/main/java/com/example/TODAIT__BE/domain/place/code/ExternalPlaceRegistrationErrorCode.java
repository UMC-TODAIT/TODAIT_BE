package com.example.TODAIT__BE.domain.place.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExternalPlaceRegistrationErrorCode implements BaseErrorCode {
    PLACE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "PLACE400", "현재 사용할 수 없는 장소입니다."),
    INVALID_PLACE_COORDINATE(HttpStatus.BAD_REQUEST, "PLACE400_5", "유효하지 않은 장소 좌표입니다."),
    DATA_SOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE404_1", "외부 장소 데이터 출처를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

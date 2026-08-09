package com.example.TODAIT__BE.domain.place.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceDetailErrorCode implements BaseErrorCode {
    PLACE_NOT_EXPOSED(HttpStatus.BAD_REQUEST, "PLACE400_4", "노출 대상이 아닌 장소입니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE404", "장소 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
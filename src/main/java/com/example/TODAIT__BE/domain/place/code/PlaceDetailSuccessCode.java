package com.example.TODAIT__BE.domain.place.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceDetailSuccessCode implements BaseSuccessCode {
    PLACE_DETAIL_OK(HttpStatus.OK, "PLACE200_1", "장소 상세 조회 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

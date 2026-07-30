package com.example.TODAIT__BE.domain.place.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceSuccessCode implements BaseSuccessCode {
    PLACE_SEARCH_OK(
            HttpStatus.OK,
            "PLACE200",
            "기준 장소 검색 결과 조회 성공"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}

package com.example.TODAIT__BE.domain.place.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceSearchErrorCode implements BaseErrorCode {
    INVALID_PLACE_SEARCH_QUERY(HttpStatus.BAD_REQUEST, "PLACE400_1", "장소 검색어를 입력해야 합니다."),
    PLACE_SEARCH_QUERY_TOO_SHORT(HttpStatus.BAD_REQUEST, "PLACE400_2", "장소 검색어는 2자 이상이어야 합니다."),
    PLACE_SEARCH_QUERY_TOO_LONG(HttpStatus.BAD_REQUEST, "PLACE400_3", "장소 검색어는 100자 이하여야 합니다."),
    KAKAO_LOCAL_API_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "PLACE429_1", "카카오 장소 검색 요청 한도를 초과했습니다."),
    KAKAO_LOCAL_API_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "PLACE502_1", "카카오 장소 검색에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
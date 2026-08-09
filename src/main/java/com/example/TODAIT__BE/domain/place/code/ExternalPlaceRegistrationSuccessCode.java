package com.example.TODAIT__BE.domain.place.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExternalPlaceRegistrationSuccessCode implements BaseSuccessCode {
    EXTERNAL_PLACE_REGISTERED(HttpStatus.CREATED, "PLACE201", "외부 장소 등록 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
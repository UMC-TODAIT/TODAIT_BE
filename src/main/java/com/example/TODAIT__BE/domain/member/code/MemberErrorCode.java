package com.example.TODAIT__BE.domain.member.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    ALREADY_REGISTERED_EMAIL(
            HttpStatus.CONFLICT,
            "MEMBER409_1",
            "이미 가입된 이메일입니다."
    ),

    ALREADY_REGISTERED_NICKNAME(
        HttpStatus.CONFLICT,
        "MEMBER409_2",
        "이미 사용중인 닉네임입니다."
    ),

    ALREADY_REGISTERED_OAUTH_ACCOUNT(
      HttpStatus.CONFLICT,
      "MEMBER409_3",
      "이미 가입된 소셜 계정입니다."
    ),

    INVALID_MEMBER_STATUS(
            HttpStatus.FORBIDDEN,
            "MEMBER403_1",
            "로그인할 수 없는 회원 상태입니다."
    ),

    INVALID_ONBOARDING_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "MEMBER401_1",
            "유효하지 않거나 만료된 온보딩 토큰입니다."
    ),

    DUPLICATE_TERM_AGREEMENT(
            HttpStatus.BAD_REQUEST,
            "MEMBER400_1",
            "동일한 약관 동의 정보가 중복되었습니다."
    ),

    REQUIRED_TERM_NOT_AGREED(
            HttpStatus.BAD_REQUEST,
            "MEMBER400_2",
            "필수 약관에 모두 동의해야 합니다."
    ),

    INVALID_TERM(
            HttpStatus.BAD_REQUEST,
            "MEMBER400_3",
            "유효하지 않거나 현재 이용할 수 없는 약관입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}

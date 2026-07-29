package com.example.TODAIT__BE.domain.member.code;


import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {

    ONBOARDING_COMPLETED(
            HttpStatus.OK,
            "MEMBER200_1",
            "소셜 회원가입이 완료되었습니다."
    ),

    SIGNUP_COMPLETED(
            HttpStatus.CREATED,
            "MEMBER201_1",
            "회원가입이 완료되었습니다."
    ),

    LOGIN_COMPLETED(
            HttpStatus.OK,
            "MEMBER200_2",
            "로그인이 완료되었습니다."
    ),

    NICKNAME_AVAILABILITY_CHECKED(
            HttpStatus.OK,
            "MEMBER200_4",
            "닉네임 중복 확인에 성공했습니다."
    ),

    MY_INFO_RETRIEVED(
            HttpStatus.OK,
            "MEMBER200_3",
            "회원 정보 조회 성공"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}

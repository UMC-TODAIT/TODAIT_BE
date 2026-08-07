package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "MEMBER", description = "로그인, 회원가입, 이메일 인증, 온보딩, 회원 정보 API")
public interface EmailVerificationControllerDocs {

    @Operation(
            summary = "[일반 회원가입] 이메일 인증 코드 발송",
            description = """
                    회원가입에 사용할 이메일 인증 코드를 발송합니다.

                    - 대상: 일반 이메일 회원가입 이메일
                    - 재발송 제한과 만료 시간은 서버 정책을 따릅니다.
                    """
    )
    ResponseEntity<ApiResponse<EmailVerificationResponse.Send>> sendVerificationCode(EmailVerificationRequest.Send request);

    @Operation(
            summary = "[일반 회원가입] 이메일 인증 코드 검증",
            description = """
                    이메일로 발송된 인증 코드를 검증합니다.

                    검증이 완료된 이메일은 일반 회원가입에 사용할 수 있습니다.
                    """
    )
    ResponseEntity<ApiResponse<EmailVerificationResponse.Verify>> verifyCode(EmailVerificationRequest.Verify request);
}

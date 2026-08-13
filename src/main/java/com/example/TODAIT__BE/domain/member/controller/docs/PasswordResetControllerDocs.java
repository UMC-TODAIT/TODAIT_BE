package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.PasswordResetRequest;
import com.example.TODAIT__BE.domain.member.dto.response.PasswordResetResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "MEMBER",
        description = "로그인, 회원가입, 이메일 인증, 온보딩, 회원 정보 API"
)
public interface PasswordResetControllerDocs {

    @Operation(
            summary = "[비밀번호 재설정, MVP 제외] 인증번호 발송",
            description = """
                    일반 이메일 회원의 비밀번호 재설정을 위해 이메일 인증번호를 발송합니다.

                    - 소셜 로그인 전용 회원은 비밀번호 재설정 대상이 아닙니다.
                    - 존재하지 않는 이메일이어도 계정 존재 여부를 노출하지 않기 위해 성공 응답을 반환합니다.
                    - 이메일별 재발송 대기 시간을 적용합니다.
                    """
    )
    ResponseEntity<ApiResponse<PasswordResetResponse.Send>> sendPasswordResetCode(PasswordResetRequest.Send request);

    @Operation(
            summary = "[비밀번호 재설정, MVP 제외] 인증번호 확인",
            description = """
                    이메일로 발송된 비밀번호 재설정 인증번호를 확인하고 새 비밀번호 설정용 resetToken을 발급합니다.

                    - 인증번호는 제한된 시간 동안만 유효합니다.
                    - 확인 시도 횟수를 초과하면 새 인증번호를 요청해야 합니다.
                    - 발급된 resetToken은 새 비밀번호 설정 API에서 사용합니다.
                    """
    )
    ResponseEntity<ApiResponse<PasswordResetResponse.Verify>> verifyPasswordResetCode(PasswordResetRequest.Verify request);

    @Operation(
            summary = "[비밀번호 재설정, MVP 제외] 새 비밀번호 설정",
            description = """
                    비밀번호 재설정 인증번호 확인 후 발급받은 resetToken으로 새 비밀번호를 설정합니다.

                    - resetToken은 일회성으로 사용됩니다.
                    - 새 비밀번호는 PasswordEncoder를 통해 해시로만 저장합니다.
                    - 비밀번호 변경 후 기존 refreshToken을 폐기합니다.
                    """
    )
    ResponseEntity<ApiResponse<PasswordResetResponse.SetNewPassword>> setNewPassword(
            PasswordResetRequest.SetNewPassword request
    );
}

package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.PasswordResetRequest;
import com.example.TODAIT__BE.domain.member.dto.response.PasswordResetResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Password Reset", description = "비밀번호 재설정 API")
public interface PasswordResetControllerDocs {

    @Operation(
            summary = "비밀번호 재설정 인증번호 발송",
            description = "일반 이메일 회원의 비밀번호 재설정을 위해 이메일 인증번호를 발송합니다."
    )
    ResponseEntity<ApiResponse<PasswordResetResponse.Send>> sendPasswordResetCode(PasswordResetRequest.Send request);

    @Operation(
            summary = "비밀번호 재설정 인증번호 확인",
            description = "이메일로 발송된 비밀번호 재설정 인증번호를 확인하고 새 비밀번호 설정용 resetToken을 발급합니다."
    )
    ResponseEntity<ApiResponse<PasswordResetResponse.Verify>> verifyPasswordResetCode(PasswordResetRequest.Verify request);

    @Operation(
            summary = "새 비밀번호 설정",
            description = "비밀번호 재설정 인증번호 확인 후 발급받은 resetToken으로 새 비밀번호를 설정합니다."
    )
    ResponseEntity<ApiResponse<PasswordResetResponse.SetNewPassword>> setNewPassword(
            PasswordResetRequest.SetNewPassword request
    );
}

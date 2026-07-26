package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.SignRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Usual Sign" ,description = "일반 회원가입/로그인 API")
@SecurityRequirements
public interface AuthControllerDocs {

    @Operation(
            summary = "일반 회원가입",
            description = """
                    이메일 인증을 완료한 사용자의 일반 회원가입을 처리합니다.
                    회원가입이 완료되면 회원과 약관 동의 내역을 저장하고
                    서비스 Access Token과 Refresh Token을 발급합니다.
                    """
    )
    ResponseEntity<ApiResponse<AuthTokenResponse.Token>> signup(
            SignRequest.SignUp request
    );

    @Operation(
            summary = "이메일 로그인",
            description = """
                이메일과 비밀번호를 검증하여 로그인합니다.
                로그인에 성공하면 서비스 Access Token과 Refresh Token을 발급합니다.
                """
    )
    ResponseEntity<ApiResponse<AuthTokenResponse.Token>> login(
            SignRequest.Login request
    );
}
package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "MEMBER",
        description = "로그인, 회원가입, 이메일 인증, 온보딩, 회원 정보 API"
)
@SecurityRequirements
public interface TokenControllerDocs {
    @Operation(
            summary = "[토큰] Access Token 및 Refresh Token 재발급",
            description = """
                    Refresh Token을 검증하여 새로운 Access Token과 Refresh Token을 발급합니다.

                    재발급에 사용한 기존 Refresh Token은 즉시 폐기되므로,
                    클라이언트는 응답으로 받은 두 토큰을 모두 교체하여 저장해야 합니다.

                    - 만료된 Refresh Token: 재로그인 필요
                    - 폐기된 Refresh Token: 재로그인 필요
                    """
    )
    ResponseEntity<ApiResponse<AuthResponse.Token>> refresh(
            AuthRequest.TokenRefresh request
    );
}

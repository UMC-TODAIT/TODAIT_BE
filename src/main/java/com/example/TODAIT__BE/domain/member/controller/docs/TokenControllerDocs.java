package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "TokenRefresh",
        description = "토큰 재발급 API"
)
@SecurityRequirements
public interface TokenControllerDocs {
    @Operation(
            summary = "Access Token 재발급",
            description = """
                    Refresh Token을 검증하여 새로운 Access Token을 발급합니다.
                   
                    만료되거나 폐기된 Refresh Token은 사용할 수 없으며,
                    이 경우 사용자는 다시 로그인해야 합니다.
                    """
    )
    ResponseEntity<ApiResponse<AuthResponse.AccessToken>> refresh(
            AuthRequest.TokenRefresh request
    );
}

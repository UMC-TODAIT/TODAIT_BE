package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.OAuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "MEMBER",
        description = "로그인, 회원가입, 이메일 인증, 온보딩, 회원 정보 API"
)
public interface OnboardingControllerDocs {

    @Operation(
            summary = "[소셜 회원가입] 소셜 온보딩 완료",
            description = """
                    신규 소셜 로그인 사용자의 닉네임과 약관 동의 정보를 검증합니다.

                    온보딩이 완료되면 회원, 소셜 계정 연결 정보, 약관 동의 내역을 저장하고
                    Access Token과 Refresh Token을 발급합니다.

                    - 인증: 카카오/구글 로그인 API에서 발급받은 Onboarding Token 사용
                    - 필수 약관: SERVICE, PRIVACY
                    - 선택 약관: LOCATION, MARKETING
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<AuthResponse.Token>> completeOnboarding(
            @Parameter(hidden = true) String authorization,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "소셜 회원가입 완료 요청 예시",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = OAuthRequest.Onboarding.class,
                                    example = """
                                        {
                                          "nickname": "투데잇소셜",
                                          "termAgreements": [
                                            {
                                              "termType": "SERVICE",
                                              "agreed": true
                                            },
                                            {
                                              "termType": "PRIVACY",
                                              "agreed": true
                                            },
                                            {
                                              "termType": "LOCATION",
                                              "agreed": false
                                            },
                                            {
                                              "termType": "MARKETING",
                                              "agreed": false
                                            }
                                          ]
                                        }
                                        """
                            )
                    )
            )
            OAuthRequest.Onboarding request
    );
}

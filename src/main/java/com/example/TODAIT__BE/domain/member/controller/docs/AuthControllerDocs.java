package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "MEMBER", description = "로그인, 회원가입, 이메일 인증, 온보딩, 회원 정보 API")
@SecurityRequirements
public interface AuthControllerDocs {

    @Operation(
            summary = "[일반 회원가입] 일반 이메일 회원가입",
            description = """
                    이메일 인증을 완료한 사용자의 일반 회원가입을 처리합니다.

                    회원가입이 완료되면 회원과 약관 동의 내역을 저장하고
                    서비스 Access Token과 Refresh Token을 발급합니다.

                    - 필수 약관: SERVICE, PRIVACY
                    - 선택 약관: LOCATION, MARKETING
                    """
    )
    ResponseEntity<ApiResponse<AuthResponse.Token>> signup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "일반 회원가입 요청 예시",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = AuthRequest.SignUp.class,
                                    example = """
                                        {
                                          "nickname": "투데잇테스트",
                                          "email": "user@example.com",
                                          "password": "Todait1234!",
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
            AuthRequest.SignUp request
    );

    @Operation(
            summary = "[일반 로그인] 이메일 로그인",
            description = """
                    이메일과 비밀번호를 검증하여 로그인합니다.

                    로그인에 성공하면 서비스 Access Token과 Refresh Token을 발급합니다.
                    """
    )
    ResponseEntity<ApiResponse<AuthResponse.Token>> login(
            AuthRequest.Login request
    );

    @Operation(
            summary = "[로그아웃] Refresh Token 로그아웃",
            description = """
                    Refresh Token을 폐기하여 로그아웃합니다.

                    폐기된 Refresh Token은 Access Token 재발급에 사용할 수 없습니다.
                    """
    )
    ResponseEntity<ApiResponse<Void>> logout(
            AuthRequest.Logout request
    );
}

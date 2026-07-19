package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.dto.request.OAuthOnboardingRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.code.MemberSuccessCode;
import com.example.TODAIT__BE.domain.member.service.OnboardingService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@Tag(
        name = "Onboarding",
        description = "카카오/구글 간편 가입 사용자의 온보딩 API"
)
public class OnboardingController {

    private static final String BEARER_PREFIX = "Bearer ";
    private final OnboardingService onboardingService;

    @PatchMapping("/api/members/me/onboarding")
    @Operation(
            summary = "소셜 회원가입 완료",
            description = """
                신규 소셜 로그인 사용자의 닉네임과 약관 동의 정보를 검증합니다.
                온보딩이 완료되면 회원, 소셜 계정 연결 정보, 약관 동의 내역을 저장하고
                Access Token과 Refresh Token을 발급합니다.
                """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    public ResponseEntity<ApiResponse<AuthTokenResponse.Token>>
    completeOnboarding(
            @Parameter(hidden = true)
            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorization,

            @Valid
            @RequestBody
            OAuthOnboardingRequest.Complete request
    ){
      String onboardingToken = extractBearerToken(authorization);

      AuthTokenResponse.Token response = onboardingService.complete(onboardingToken,request);

      return ResponseEntity.ok(
              ApiResponse.onSuccess(MemberSuccessCode.ONBOARDING_COMPLETED,response)
      );


    }


    private String extractBearerToken(String authorization){
        if(authorization == null || !authorization.startsWith(BEARER_PREFIX)){
            throw new MemberException(MemberErrorCode.INVALID_ONBOARDING_TOKEN);
        }

        String token = authorization.substring(BEARER_PREFIX.length());

        if(token.isBlank()){
            throw new MemberException(MemberErrorCode.INVALID_ONBOARDING_TOKEN);
        }

        return token;
    }


}

package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.dto.request.OAuthOnboardingRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.code.MemberSuccessCode;
import com.example.TODAIT__BE.domain.member.service.OnboardingService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
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
public class OnboardingController {

    private static final String BEARER_PREFIX = "Bearer ";
    private final OnboardingService onboardingService;

    @PatchMapping("/api/members/me/onboarding")
    public ResponseEntity<ApiResponse<AuthTokenResponse.Token>>
    completeOnboarding(
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

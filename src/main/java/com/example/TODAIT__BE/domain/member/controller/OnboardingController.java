package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.controller.docs.OnboardingControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.request.OAuthOnboardingRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.code.MemberSuccessCode;
import com.example.TODAIT__BE.domain.member.service.OnboardingService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.JwtBearerTokenExtractor;
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
public class OnboardingController implements OnboardingControllerDocs {

    private final OnboardingService onboardingService;

    @PatchMapping("/api/members/me/onboarding")
    @Override
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
      String onboardingToken = JwtBearerTokenExtractor.extract(authorization)
              .orElseThrow(() -> new MemberException(
                      MemberErrorCode.INVALID_ONBOARDING_TOKEN
              ));

      AuthTokenResponse.Token response = onboardingService.complete(onboardingToken,request);

      return ResponseEntity
              .status(MemberSuccessCode.ONBOARDING_COMPLETED.getStatus())
              .body(ApiResponse.onSuccess(MemberSuccessCode.ONBOARDING_COMPLETED, response));


    }
}

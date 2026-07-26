package com.example.TODAIT__BE.domain.member.service;


import com.example.TODAIT__BE.domain.member.dto.request.OAuthOnboardingRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import com.example.TODAIT__BE.global.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final TermAgreementValidator termAgreementValidator;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberDuplicateValidator memberDuplicateValidator;

    @Transactional
    public AuthTokenResponse.Token complete(
        String onboardingToken,
        OAuthOnboardingRequest.Complete request
    ){
        //온보딩 토큰 검증
        if(!jwtTokenProvider.validateToken(onboardingToken)
                || !jwtTokenProvider.isOAuthOnboardingToken(onboardingToken)){
            throw new MemberException(MemberErrorCode.INVALID_ONBOARDING_TOKEN);
        }


        String providerUserId = jwtTokenProvider.getSubject(onboardingToken);
        OAuthProvider provider = jwtTokenProvider.getOAuthProvider(onboardingToken);
        String email = MemberInputPolicy.normalizeEmail(
                jwtTokenProvider.getEmail(onboardingToken)
        );
        String nickname = MemberInputPolicy.normalizeNickname(request.nickname());


        memberDuplicateValidator.validateNicknameAvailable(nickname);
        memberDuplicateValidator.validateOAuthAccountAvailable(provider, providerUserId);
        memberDuplicateValidator.validateEmailAvailable(email);

        List<Term> agreedTerms =
                termAgreementValidator.validateAndGetAgreedTerms(
                        request.termAgreements()
                );

        LocalDateTime now = LocalDateTime.now();

        Member savedMember = memberRegistrationService.saveMember(
                Member.builder()
                        .email(email)
                        .nickname(nickname)
                        .build()
        );

        memberRegistrationService.saveOAuthAccount(
                savedMember,
                provider,
                providerUserId,
                email,
                now
        );

        memberRegistrationService.saveTermAgreements(savedMember, agreedTerms, now);

        return  authService.issueTokens(savedMember);
    }
}

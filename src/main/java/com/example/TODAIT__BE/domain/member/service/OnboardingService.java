package com.example.TODAIT__BE.domain.member.service;


import com.example.TODAIT__BE.domain.member.dto.request.OAuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final AuthService authService;
    private final TermAgreementValidator termAgreementValidator;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberDuplicateValidator memberDuplicateValidator;

    @Transactional
    public AuthResponse.Token complete(
        String onboardingToken,
        OAuthRequest.Onboarding request
    ){
        AuthService.OAuthOnboardingTokenClaims claims =
                authService.validateOAuthOnboardingToken(onboardingToken);
        String providerUserId = claims.providerUserId();
        OAuthProvider provider = claims.provider();
        String email = MemberInputPolicy.normalizeEmail(
                claims.email()
        );
        String nickname = request.nickname();

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

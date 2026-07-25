package com.example.TODAIT__BE.domain.member.service;


import com.example.TODAIT__BE.domain.member.dto.request.OAuthOnboardingRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.entity.MemberTermAgreement;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exception.MemberIntegrityViolationMapper;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberTermAgreementRepository;
import com.example.TODAIT__BE.domain.member.support.MemberInputNormalizer;
import com.example.TODAIT__BE.global.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final MemberOAuthAccountRepository memberOAuthAccountRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;
    private final AuthService authService;
    private final TermAgreementValidator termAgreementValidator;
    private final MemberIntegrityViolationMapper integrityViolationMapper;

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
        String email = MemberInputNormalizer.normalizeEmail(
                jwtTokenProvider.getEmail(onboardingToken)
        );
        String nickname = MemberInputNormalizer.normalizeNickname(request.nickname());


        if(memberRepository.existsByNickname(nickname)){
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_NICKNAME);
        }

        if(memberOAuthAccountRepository.existsByProviderAndProviderUserId(provider,providerUserId)){
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT);
        }

        if(email != null && memberRepository.existsByEmail(email)){
            throw  new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        List<Term> agreedTerms =
                termAgreementValidator.validateAndGetAgreedTerms(
                        request.termAgreements()
                );

        LocalDateTime now = LocalDateTime.now();

        Member savedMember;
        try {
            savedMember = memberRepository.saveAndFlush(
                    Member.builder()
                            .email(email)
                            .nickname(nickname)
                            .build()
            );
        } catch (DataIntegrityViolationException exception) {
            throw integrityViolationMapper.mapMemberSaveException(exception);
        }

        try {
            memberOAuthAccountRepository.saveAndFlush(
                    MemberOAuthAccount.builder()
                            .member(savedMember)
                            .provider(provider)
                            .providerUserId(providerUserId)
                            .providerEmail(email)
                            .linkedAt(now)
                            .build()
            );
        } catch (DataIntegrityViolationException exception) {
            if (integrityViolationMapper.hasConstraint(
                    exception,
                    "uk_member_oauth_provider_user"
            )) {
                throw new MemberException(
                        MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT
                );
            }
            throw exception;
        }

        List<MemberTermAgreement> agreements = new ArrayList<>();

        for (Term term : agreedTerms) {
            agreements.add(
                    MemberTermAgreement.builder()
                            .member(savedMember)
                            .term(term)
                            .agreed(true)
                            .agreedAt(now)
                            .build()
            );
        }

        memberTermAgreementRepository.saveAll(agreements);

        return  authService.issueTokens(savedMember);
    }
}

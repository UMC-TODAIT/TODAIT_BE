package com.example.TODAIT__BE.domain.member.service;


import com.example.TODAIT__BE.domain.member.dto.request.OAuthOnboardingRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.entity.MemberTermAgreement;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.enums.TermType;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberTermAgreementRepository;
import com.example.TODAIT__BE.domain.member.repository.TermRepository;
import com.example.TODAIT__BE.global.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final MemberOAuthAccountRepository memberOAuthAccountRepository;
    private final TermRepository termRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;
    private final AuthService authService;

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
        String email = jwtTokenProvider.getEmail(onboardingToken);


        if(memberRepository.existsByNickname(request.nickname())){
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_NICKNAME);
        }

        if(memberOAuthAccountRepository.existsByProviderAndProviderUserId(provider,providerUserId)){
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT);
        }

        if(email != null && memberRepository.existsByEmail(email)){
            throw  new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL);
        }

        //약관 요청 TermType별 Map으로 변환
        Map<TermType, Boolean> agreementMap = new EnumMap<>(TermType.class);

        for(OAuthOnboardingRequest.TermAgreement agreement
                : request.termAgreements()){
            if(agreementMap.containsKey(agreement.termType())){
                throw new MemberException(
                        MemberErrorCode.DUPLICATE_TERM_AGREEMENT
                );
            }
            agreementMap.put(
                    agreement.termType(),
                    agreement.agreed()
            );
        }

        List<Term> activeTerms = termRepository.findAllByIsActiveTrue();

        Set<TermType> activeTermTypes = new HashSet<>();

        for (Term term : activeTerms) {
            activeTermTypes.add(term.getTermType());
        }

        // 프론트가 보낸 약관이 활성약관인지
        for (TermType requestedType : agreementMap.keySet()){
            if(!activeTermTypes.contains(requestedType)){
                throw new MemberException(
                        MemberErrorCode.INVALID_TERM
                );
            }
        }

        // 필수약관 동의 검사
        for(Term term : activeTerms){
            if(term.isRequired() && !Boolean.TRUE.equals(agreementMap.get(term.getTermType())
            )){
                throw  new MemberException(MemberErrorCode.REQUIRED_TERM_NOT_AGREED);
            }
        }

        LocalDateTime now = LocalDateTime.now();

        Member savedMember = memberRepository.save(
                Member.builder()
                        .email(email)
                        .nickname(request.nickname())
                        .build()
        );

        memberOAuthAccountRepository.save(
                MemberOAuthAccount.builder()
                        .member(savedMember)
                        .provider(provider)
                        .providerUserId(providerUserId)
                        .providerEmail(email)
                        .linkedAt(now)
                        .build()
        );

        List<MemberTermAgreement> agreements = new ArrayList<>();

        for (Term term : activeTerms) {
            if (Boolean.TRUE.equals(
                    agreementMap.get(term.getTermType())
            )) {
                agreements.add(
                        MemberTermAgreement.builder()
                                .member(savedMember)
                                .term(term)
                                .agreed(true)
                                .agreedAt(now)
                                .build()
                );
            }
        }

        memberTermAgreementRepository.saveAll(agreements);

        return  authService.issueTokens(savedMember);
    }
}

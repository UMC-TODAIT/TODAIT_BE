package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.entity.MemberTermAgreement;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exception.MemberIntegrityViolationMapper;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberTermAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberRegistrationService {

    private final MemberRepository memberRepository;
    private final MemberOAuthAccountRepository memberOAuthAccountRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;
    private final MemberIntegrityViolationMapper integrityViolationMapper;

    public Member saveMember(Member member) {
        try {
            return memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException exception) {
            throw integrityViolationMapper.mapMemberSaveException(exception);
        }
    }

    public void saveTermAgreements(
            Member member,
            List<Term> agreedTerms,
            LocalDateTime agreedAt
    ) {
        List<MemberTermAgreement> agreements =
                agreedTerms.stream()
                        .map(term ->
                                MemberTermAgreement.builder()
                                        .member(member)
                                        .term(term)
                                        .agreed(true)
                                        .agreedAt(agreedAt)
                                        .build()
                        )
                        .toList();

        memberTermAgreementRepository.saveAll(agreements);
    }

    public MemberOAuthAccount saveOAuthAccount(
            Member member,
            OAuthProvider provider,
            String providerUserId,
            String providerEmail,
            LocalDateTime linkedAt
    ) {
        try {
            return memberOAuthAccountRepository.saveAndFlush(
                    MemberOAuthAccount.builder()
                            .member(member)
                            .provider(provider)
                            .providerUserId(providerUserId)
                            .providerEmail(providerEmail)
                            .linkedAt(linkedAt)
                            .build()
            );
        } catch (DataIntegrityViolationException exception) {
            throw integrityViolationMapper.mapOAuthAccountSaveException(exception);
        }
    }
}

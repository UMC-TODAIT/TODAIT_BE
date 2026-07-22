package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.TermAgreementRequest;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.TermType;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TermAgreementValidator {

    private final TermRepository termRepository;

    public List<Term> validateAndGetAgreedTerms(
            List<TermAgreementRequest> termAgreements
    ) {
        Map<TermType, Boolean> agreementMap = createAgreementMap(termAgreements);
        Map<TermType, Term> activeTermMap = createActiveTermMap();

        validateRequestedTerms(agreementMap, activeTermMap);
        validateRequiredTerms(agreementMap, activeTermMap);

        List<Term> agreedTerms = new ArrayList<>();
        for (Term term : activeTermMap.values()) {
            if (Boolean.TRUE.equals(agreementMap.get(term.getTermType()))) {
                agreedTerms.add(term);
            }
        }
        return agreedTerms;
    }

    private Map<TermType, Boolean> createAgreementMap(
            List<TermAgreementRequest> termAgreements
    ) {
        Map<TermType, Boolean> agreementMap = new EnumMap<>(TermType.class);

        for (TermAgreementRequest agreement : termAgreements) {
            if (agreementMap.containsKey(agreement.termType())) {
                throw new MemberException(
                        MemberErrorCode.DUPLICATE_TERM_AGREEMENT
                );
            }
            agreementMap.put(agreement.termType(), agreement.agreed());
        }
        return agreementMap;
    }

    private Map<TermType, Term> createActiveTermMap() {
        Map<TermType, Term> activeTermMap = new EnumMap<>(TermType.class);

        for (Term term : termRepository.findAllByIsActiveTrue()) {
            Term previous = activeTermMap.putIfAbsent(term.getTermType(), term);
            if (previous != null) {
                throw new IllegalStateException(
                        "동일한 타입의 활성 약관이 여러 개 등록되어 있습니다."
                );
            }
        }
        return activeTermMap;
    }

    private void validateRequestedTerms(
            Map<TermType, Boolean> agreementMap,
            Map<TermType, Term> activeTermMap
    ) {
        for (TermType requestedType : agreementMap.keySet()) {
            if (!activeTermMap.containsKey(requestedType)) {
                throw new MemberException(MemberErrorCode.INVALID_TERM);
            }
        }
    }

    private void validateRequiredTerms(
            Map<TermType, Boolean> agreementMap,
            Map<TermType, Term> activeTermMap
    ) {
        for (Term term : activeTermMap.values()) {
            if (term.isRequired()
                    && !Boolean.TRUE.equals(
                    agreementMap.get(term.getTermType())
            )) {
                throw new MemberException(
                        MemberErrorCode.REQUIRED_TERM_NOT_AGREED
                );
            }
        }
    }
}

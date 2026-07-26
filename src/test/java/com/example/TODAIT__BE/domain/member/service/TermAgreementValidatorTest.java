package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.TermAgreementRequest;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.TermType;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.TermRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TermAgreementValidatorTest {

    @Mock
    private TermRepository termRepository;

    private TermAgreementValidator termAgreementValidator;

    @BeforeEach
    void setUp() {
        termAgreementValidator = new TermAgreementValidator(termRepository);
    }

    @Test
    void validateAndGetAgreedTermsReturnsOnlyAgreedActiveTerms() {
        Term serviceTerm = term(TermType.SERVICE, true);
        Term privacyTerm = term(TermType.PRIVACY, true);
        Term marketingTerm = term(TermType.MARKETING, false);
        given(termRepository.findAllByIsActiveTrue())
                .willReturn(List.of(serviceTerm, privacyTerm, marketingTerm));

        List<Term> agreedTerms = termAgreementValidator.validateAndGetAgreedTerms(
                List.of(
                        new TermAgreementRequest(TermType.SERVICE, true),
                        new TermAgreementRequest(TermType.PRIVACY, true),
                        new TermAgreementRequest(TermType.MARKETING, false)
                )
        );

        assertThat(agreedTerms)
                .containsExactlyInAnyOrder(serviceTerm, privacyTerm)
                .doesNotContain(marketingTerm);
    }

    @Test
    void validateAndGetAgreedTermsRejectsDuplicateTermType() {
        assertThatThrownBy(() -> termAgreementValidator.validateAndGetAgreedTerms(
                List.of(
                        new TermAgreementRequest(TermType.SERVICE, true),
                        new TermAgreementRequest(TermType.SERVICE, true)
                )
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.DUPLICATE_TERM_AGREEMENT);
    }

    @Test
    void validateAndGetAgreedTermsRejectsInactiveOrUnknownTermType() {
        given(termRepository.findAllByIsActiveTrue())
                .willReturn(List.of(term(TermType.SERVICE, true)));

        assertThatThrownBy(() -> termAgreementValidator.validateAndGetAgreedTerms(
                List.of(
                        new TermAgreementRequest(TermType.SERVICE, true),
                        new TermAgreementRequest(TermType.PRIVACY, true)
                )
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_TERM);
    }

    @Test
    void validateAndGetAgreedTermsRejectsMissingRequiredAgreement() {
        given(termRepository.findAllByIsActiveTrue())
                .willReturn(List.of(
                        term(TermType.SERVICE, true),
                        term(TermType.PRIVACY, true)
                ));

        assertThatThrownBy(() -> termAgreementValidator.validateAndGetAgreedTerms(
                List.of(new TermAgreementRequest(TermType.SERVICE, true))
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.REQUIRED_TERM_NOT_AGREED);
    }

    @Test
    void validateAndGetAgreedTermsRejectsMultipleActiveTermsOfSameType() {
        given(termRepository.findAllByIsActiveTrue())
                .willReturn(List.of(
                        term(TermType.SERVICE, true),
                        term(TermType.SERVICE, true)
                ));

        assertThatThrownBy(() -> termAgreementValidator.validateAndGetAgreedTerms(
                List.of(new TermAgreementRequest(TermType.SERVICE, true))
        ))
                .isInstanceOf(IllegalStateException.class);
    }

    private Term term(TermType termType, boolean required) {
        return Term.builder()
                .termType(termType)
                .title(termType.name())
                .content("content")
                .version("1.0")
                .isRequired(required)
                .isActive(true)
                .build();
    }
}

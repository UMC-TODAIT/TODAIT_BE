package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.OAuthOnboardingRequest;
import com.example.TODAIT__BE.domain.member.dto.request.TermAgreementRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.enums.TermType;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private AuthService authService;
    @Mock
    private TermAgreementValidator termAgreementValidator;
    @Mock
    private MemberRegistrationService memberRegistrationService;
    @Mock
    private MemberDuplicateValidator memberDuplicateValidator;

    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        onboardingService = new OnboardingService(
                authService,
                termAgreementValidator,
                memberRegistrationService,
                memberDuplicateValidator
        );
    }

    @Test
    void completeRejectsInvalidOnboardingToken() {
        OAuthOnboardingRequest.Complete request = onboardingRequest("tester");
        willThrow(new MemberException(MemberErrorCode.INVALID_ONBOARDING_TOKEN))
                .given(authService)
                .validateOAuthOnboardingToken("token");

        assertThatThrownBy(() -> onboardingService.complete("token", request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_ONBOARDING_TOKEN);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void completeCreatesMemberOauthAccountTermAgreementsAndTokens() {
        OAuthOnboardingRequest.Complete request = onboardingRequest(" tester ");
        Term serviceTerm = term(TermType.SERVICE);
        List<Term> agreedTerms = List.of(serviceTerm);
        Member savedMember = Member.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("tester")
                .build();
        AuthTokenResponse.Token token = new AuthTokenResponse.Token("access", "refresh");

        givenValidOnboardingToken("token");
        given(termAgreementValidator.validateAndGetAgreedTerms(request.termAgreements()))
                .willReturn(agreedTerms);
        given(memberRegistrationService.saveMember(any(Member.class))).willReturn(savedMember);
        given(authService.issueTokens(savedMember)).willReturn(token);

        AuthTokenResponse.Token response = onboardingService.complete("token", request);

        assertThat(response).isEqualTo(token);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRegistrationService).saveMember(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(memberCaptor.getValue().getNickname()).isEqualTo("tester");

        verify(memberRegistrationService).saveOAuthAccount(
                eq(savedMember),
                eq(OAuthProvider.GOOGLE),
                eq("provider-user-id"),
                eq("user@example.com"),
                any(LocalDateTime.class)
        );

        verify(memberRegistrationService).saveTermAgreements(
                eq(savedMember),
                eq(agreedTerms),
                any(LocalDateTime.class)
        );
    }

    @Test
    void completeRejectsDuplicateNickname() {
        OAuthOnboardingRequest.Complete request = onboardingRequest("tester");
        givenValidOnboardingToken("token");
        willThrow(new MemberException(MemberErrorCode.ALREADY_REGISTERED_NICKNAME))
                .given(memberDuplicateValidator)
                .validateNicknameAvailable("tester");

        assertThatThrownBy(() -> onboardingService.complete("token", request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_NICKNAME);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    private void givenValidOnboardingToken(String token) {
        given(authService.validateOAuthOnboardingToken(token))
                .willReturn(new AuthService.OAuthOnboardingTokenClaims(
                        OAuthProvider.GOOGLE,
                        "provider-user-id",
                        " User@Example.com "
                ));
    }

    private OAuthOnboardingRequest.Complete onboardingRequest(String nickname) {
        return new OAuthOnboardingRequest.Complete(
                nickname,
                List.of(new TermAgreementRequest(TermType.SERVICE, true))
        );
    }

    private Term term(TermType termType) {
        return Term.builder()
                .termType(termType)
                .title(termType.name())
                .content("content")
                .version("1.0")
                .isRequired(true)
                .isActive(true)
                .build();
    }
}

package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.OAuthOnboardingRequest;
import com.example.TODAIT__BE.domain.member.dto.request.TermAgreementRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.enums.TermType;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.exception.MemberIntegrityViolationMapper;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberOAuthAccountRepository memberOAuthAccountRepository;
    @Mock
    private AuthService authService;
    @Mock
    private TermAgreementValidator termAgreementValidator;
    @Mock
    private MemberIntegrityViolationMapper integrityViolationMapper;
    @Mock
    private MemberRegistrationService memberRegistrationService;

    private OnboardingService onboardingService;

    @BeforeEach
    void setUp() {
        onboardingService = new OnboardingService(
                jwtTokenProvider,
                memberRepository,
                memberOAuthAccountRepository,
                authService,
                termAgreementValidator,
                integrityViolationMapper,
                memberRegistrationService
        );
    }

    @Test
    void completeRejectsInvalidOnboardingToken() {
        OAuthOnboardingRequest.Complete request = onboardingRequest("tester");
        given(jwtTokenProvider.validateToken("token")).willReturn(false);

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
        given(jwtTokenProvider.getSubject("token")).willReturn("provider-user-id");
        given(jwtTokenProvider.getOAuthProvider("token")).willReturn(OAuthProvider.GOOGLE);
        given(jwtTokenProvider.getEmail("token")).willReturn(" User@Example.com ");
        given(memberRepository.existsByNickname("tester")).willReturn(false);
        given(memberOAuthAccountRepository.existsByProviderAndProviderUserId(
                OAuthProvider.GOOGLE,
                "provider-user-id"
        )).willReturn(false);
        given(memberRepository.existsByEmail("user@example.com")).willReturn(false);
        given(termAgreementValidator.validateAndGetAgreedTerms(request.termAgreements()))
                .willReturn(agreedTerms);
        given(memberRegistrationService.saveMember(any(Member.class))).willReturn(savedMember);
        given(memberOAuthAccountRepository.saveAndFlush(any(MemberOAuthAccount.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(authService.issueTokens(savedMember)).willReturn(token);

        AuthTokenResponse.Token response = onboardingService.complete("token", request);

        assertThat(response).isEqualTo(token);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRegistrationService).saveMember(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(memberCaptor.getValue().getNickname()).isEqualTo("tester");

        ArgumentCaptor<MemberOAuthAccount> accountCaptor =
                ArgumentCaptor.forClass(MemberOAuthAccount.class);
        verify(memberOAuthAccountRepository).saveAndFlush(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getMember()).isEqualTo(savedMember);
        assertThat(accountCaptor.getValue().getProvider()).isEqualTo(OAuthProvider.GOOGLE);
        assertThat(accountCaptor.getValue().getProviderUserId()).isEqualTo("provider-user-id");
        assertThat(accountCaptor.getValue().getProviderEmail()).isEqualTo("user@example.com");

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
        given(jwtTokenProvider.getSubject("token")).willReturn("provider-user-id");
        given(jwtTokenProvider.getOAuthProvider("token")).willReturn(OAuthProvider.GOOGLE);
        given(jwtTokenProvider.getEmail("token")).willReturn("user@example.com");
        given(memberRepository.existsByNickname("tester")).willReturn(true);

        assertThatThrownBy(() -> onboardingService.complete("token", request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_NICKNAME);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void completeMapsOauthUniqueConstraintViolation() {
        OAuthOnboardingRequest.Complete request = onboardingRequest("tester");
        Term serviceTerm = term(TermType.SERVICE);
        Member savedMember = Member.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("tester")
                .build();
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate");

        givenValidOnboardingToken("token");
        given(jwtTokenProvider.getSubject("token")).willReturn("provider-user-id");
        given(jwtTokenProvider.getOAuthProvider("token")).willReturn(OAuthProvider.GOOGLE);
        given(jwtTokenProvider.getEmail("token")).willReturn("user@example.com");
        given(memberRepository.existsByNickname("tester")).willReturn(false);
        given(memberOAuthAccountRepository.existsByProviderAndProviderUserId(
                OAuthProvider.GOOGLE,
                "provider-user-id"
        )).willReturn(false);
        given(memberRepository.existsByEmail("user@example.com")).willReturn(false);
        given(termAgreementValidator.validateAndGetAgreedTerms(request.termAgreements()))
                .willReturn(List.of(serviceTerm));
        given(memberRegistrationService.saveMember(any(Member.class))).willReturn(savedMember);
        given(memberOAuthAccountRepository.saveAndFlush(any(MemberOAuthAccount.class)))
                .willThrow(exception);
        given(integrityViolationMapper.hasConstraint(
                exception,
                "uk_member_oauth_provider_user"
        )).willReturn(true);

        assertThatThrownBy(() -> onboardingService.complete("token", request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT);
    }

    private void givenValidOnboardingToken(String token) {
        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.isOAuthOnboardingToken(token)).willReturn(true);
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

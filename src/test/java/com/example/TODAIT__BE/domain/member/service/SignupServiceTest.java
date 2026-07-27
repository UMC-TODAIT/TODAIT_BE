package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.SignRequest;
import com.example.TODAIT__BE.domain.member.dto.request.TermAgreementRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.TermType;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.infra.redis.EmailVerificationRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class SignupServiceTest {

    @Mock
    private EmailVerificationRedisRepository emailVerificationRedisRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthService authService;
    @Mock
    private TermAgreementValidator termAgreementValidator;
    @Mock
    private MemberRegistrationService memberRegistrationService;
    @Mock
    private MemberDuplicateValidator memberDuplicateValidator;

    private SignupService signupService;

    @BeforeEach
    void setUp() {
        signupService = new SignupService(
                emailVerificationRedisRepository,
                passwordEncoder,
                authService,
                termAgreementValidator,
                memberRegistrationService,
                memberDuplicateValidator
        );
    }

    @Test
    void signupNormalizesInputAndIssuesTokens() {
        SignRequest.SignUp request = signupRequest(" Tester@Example.com ", " tester ");
        List<Term> agreedTerms = List.of(term(TermType.SERVICE));
        Member savedMember = Member.builder()
                .id(1L)
                .email("tester@example.com")
                .nickname("tester")
                .passwordHash("encoded-password")
                .build();
        AuthTokenResponse.Token token = new AuthTokenResponse.Token("access", "refresh");

        given(emailVerificationRedisRepository.isVerified("tester@example.com")).willReturn(true);
        given(termAgreementValidator.validateAndGetAgreedTerms(request.termAgreements()))
                .willReturn(agreedTerms);
        given(passwordEncoder.encode("password!1")).willReturn("encoded-password");
        given(memberRegistrationService.saveMember(any(Member.class))).willReturn(savedMember);
        given(authService.issueTokens(savedMember)).willReturn(token);

        AuthTokenResponse.Token response = signupService.signup(request);

        assertThat(response).isEqualTo(token);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRegistrationService).saveMember(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getEmail()).isEqualTo("tester@example.com");
        assertThat(memberCaptor.getValue().getNickname()).isEqualTo("tester");
        assertThat(memberCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        verify(memberRegistrationService).saveTermAgreements(
                eq(savedMember),
                eq(agreedTerms),
                any(LocalDateTime.class)
        );
    }

    @Test
    void signupRejectsUnverifiedEmail() {
        SignRequest.SignUp request = signupRequest("test@example.com", "tester");
        given(emailVerificationRedisRepository.isVerified("test@example.com")).willReturn(false);

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.EMAIL_VERIFICATION_REQUIRED);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void signupRejectsDuplicateEmail() {
        SignRequest.SignUp request = signupRequest("test@example.com", "tester");
        given(emailVerificationRedisRepository.isVerified("test@example.com")).willReturn(true);
        givenDuplicateEmail("test@example.com");

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_EMAIL);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void signupRejectsDuplicateNickname() {
        SignRequest.SignUp request = signupRequest("test@example.com", "tester");
        given(emailVerificationRedisRepository.isVerified("test@example.com")).willReturn(true);
        givenDuplicateNickname("tester");

        assertThatThrownBy(() -> signupService.signup(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_NICKNAME);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    private SignRequest.SignUp signupRequest(String email, String nickname) {
        return new SignRequest.SignUp(
                nickname,
                email,
                "password!1",
                List.of(new TermAgreementRequest(TermType.SERVICE, true))
        );
    }

    private void givenDuplicateEmail(String email) {
        willThrow(new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL))
                .given(memberDuplicateValidator)
                .validateEmailAvailable(email);
    }

    private void givenDuplicateNickname(String nickname) {
        willThrow(new MemberException(MemberErrorCode.ALREADY_REGISTERED_NICKNAME))
                .given(memberDuplicateValidator)
                .validateNicknameAvailable(nickname);
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

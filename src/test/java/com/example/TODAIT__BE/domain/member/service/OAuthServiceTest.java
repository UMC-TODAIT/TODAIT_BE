package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.OAuthRequest;
import com.example.TODAIT__BE.domain.member.dto.request.TermAgreementRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.enums.TermType;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserClient;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserInfo;
import com.example.TODAIT__BE.domain.member.service.support.MemberRegistrationService;
import com.example.TODAIT__BE.domain.member.service.validator.MemberDuplicateValidator;
import com.example.TODAIT__BE.domain.member.service.validator.MemberLoginValidator;
import com.example.TODAIT__BE.domain.member.service.validator.TermAgreementValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

    @Mock
    private MemberOAuthAccountRepository memberOAuthAccountRepository;
    @Mock
    private AuthService authService;
    @Mock
    private OAuthUserClient kakaoOAuthUserClient;
    @Mock
    private OAuthUserClient googleOAuthUserClient;
    @Mock
    private MemberLoginValidator memberLoginValidator;
    @Mock
    private MemberDuplicateValidator memberDuplicateValidator;
    @Mock
    private TermAgreementValidator termAgreementValidator;
    @Mock
    private MemberRegistrationService memberRegistrationService;

    private OAuthService oAuthService;

    @BeforeEach
    void setUp() {
        given(kakaoOAuthUserClient.supports()).willReturn(OAuthProvider.KAKAO);
        given(googleOAuthUserClient.supports()).willReturn(OAuthProvider.GOOGLE);
        oAuthService = new OAuthService(
                memberOAuthAccountRepository,
                authService,
                List.of(kakaoOAuthUserClient, googleOAuthUserClient),
                memberLoginValidator,
                memberDuplicateValidator,
                termAgreementValidator,
                memberRegistrationService
        );
    }

    @Test
    void loginWithKakaoRequiresOnboardingWithNormalizedEmail() {
        given(kakaoOAuthUserClient.getUserInfo("kakao-token"))
                .willReturn(new OAuthUserInfo(
                        "provider-user-id",
                        " User@Example.com ",
                        "https://example.com/profile.jpg"
                ));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.empty());
        given(authService.issueOAuthOnboardingToken(
                OAuthProvider.KAKAO,
                "provider-user-id",
                "user@example.com",
                "https://example.com/profile.jpg"
        )).willReturn("onboarding-token");

        OAuthResponse.Login response = oAuthService.loginWithKakao("kakao-token");

        assertThat(response.loginStatus()).isEqualTo("ONBOARDING_REQUIRED");
        assertThat(response.onboardingToken()).isEqualTo("onboarding-token");
        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.profileImageUrl())
                .isEqualTo("https://example.com/profile.jpg");
        verify(memberDuplicateValidator).validateEmailAvailable("user@example.com");
    }

    @Test
    void loginWithKakaoLogsInExistingActiveMember() {
        Member member = Member.builder()
                .id(1L)
                .email("member@example.com")
                .nickname("member")
                .profileImageUrl("https://example.com/saved-profile.jpg")
                .status(MemberStatus.ACTIVE)
                .build();
        MemberOAuthAccount account = MemberOAuthAccount.builder()
                .member(member)
                .provider(OAuthProvider.KAKAO)
                .providerUserId("provider-user-id")
                .build();
        AuthResponse.Token token = new AuthResponse.Token("access", "refresh");

        given(kakaoOAuthUserClient.getUserInfo("kakao-token"))
                .willReturn(new OAuthUserInfo("provider-user-id", "member@example.com", null));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.of(account));
        given(authService.issueTokens(member)).willReturn(token);

        OAuthResponse.Login response = oAuthService.loginWithKakao("kakao-token");

        assertThat(response.loginStatus()).isEqualTo("LOGIN_COMPLETED");
        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.email()).isEqualTo("member@example.com");
        assertThat(response.profileImageUrl())
                .isEqualTo("https://example.com/saved-profile.jpg");
    }

    @Test
    void loginWithKakaoReturnsNullProfileImageWhenNotProvided() {
        given(kakaoOAuthUserClient.getUserInfo("kakao-token"))
                .willReturn(new OAuthUserInfo(
                        "provider-user-id",
                        "user@example.com",
                        null
                ));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.empty());
        given(authService.issueOAuthOnboardingToken(
                OAuthProvider.KAKAO,
                "provider-user-id",
                "user@example.com",
                null
        )).willReturn("onboarding-token");

        OAuthResponse.Login response = oAuthService.loginWithKakao("kakao-token");

        assertThat(response.loginStatus()).isEqualTo("ONBOARDING_REQUIRED");
        assertThat(response.profileImageUrl()).isNull();
    }

    @Test
    void loginWithKakaoRejectsInactiveExistingMember() {
        Member member = Member.builder()
                .email("member@example.com")
                .nickname("member")
                .status(MemberStatus.BLOCKED)
                .build();
        MemberOAuthAccount account = MemberOAuthAccount.builder()
                .member(member)
                .provider(OAuthProvider.KAKAO)
                .providerUserId("provider-user-id")
                .build();

        given(kakaoOAuthUserClient.getUserInfo("kakao-token"))
                .willReturn(new OAuthUserInfo("provider-user-id", "member@example.com",null));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.of(account));
        willThrow(new MemberException(MemberErrorCode.INVALID_MEMBER_STATUS))
                .given(memberLoginValidator)
                .validateLoginAvailable(member);

        assertThatThrownBy(() -> oAuthService.loginWithKakao("kakao-token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_MEMBER_STATUS);
    }

    @Test
    void loginWithKakaoRejectsEmailAlreadyRegisteredByEmailSignup() {
        given(kakaoOAuthUserClient.getUserInfo("kakao-token"))
                .willReturn(new OAuthUserInfo("provider-user-id", "User@Example.com",null));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.empty());
        willThrow(new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL))
                .given(memberDuplicateValidator)
                .validateEmailAvailable("user@example.com");

        assertThatThrownBy(() -> oAuthService.loginWithKakao("kakao-token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_EMAIL);
    }

    @Test
    void constructorRejectsDuplicateOAuthProviderClients() {
        OAuthUserClient duplicateKakaoOAuthUserClient = mock(OAuthUserClient.class);
        given(duplicateKakaoOAuthUserClient.supports()).willReturn(OAuthProvider.KAKAO);

        assertThatThrownBy(() -> new OAuthService(
                memberOAuthAccountRepository,
                authService,
                List.of(kakaoOAuthUserClient, duplicateKakaoOAuthUserClient, googleOAuthUserClient),
                memberLoginValidator,
                memberDuplicateValidator,
                termAgreementValidator,
                memberRegistrationService
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate OAuth client configured");
    }

    @Test
    void constructorRejectsMissingOAuthProviderClient() {
        assertThatThrownBy(() -> new OAuthService(
                memberOAuthAccountRepository,
                authService,
                List.of(kakaoOAuthUserClient),
                memberLoginValidator,
                memberDuplicateValidator,
                termAgreementValidator,
                memberRegistrationService
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OAuth client is not configured");
    }

    @Test
    void completeOnboardingRejectsInvalidOnboardingToken() {
        OAuthRequest.Onboarding request = onboardingRequest("tester");
        willThrow(new MemberException(MemberErrorCode.INVALID_ONBOARDING_TOKEN))
                .given(authService)
                .validateOAuthOnboardingToken("token");

        assertThatThrownBy(() -> oAuthService.completeOnboarding("token", request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_ONBOARDING_TOKEN);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void completeOnboardingCreatesMemberOauthAccountTermAgreementsAndTokens() {
        OAuthRequest.Onboarding request = onboardingRequest(" tester ");
        Term serviceTerm = term(TermType.SERVICE);
        List<Term> agreedTerms = List.of(serviceTerm);
        Member savedMember = Member.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("tester")
                .build();
        AuthResponse.Token token = new AuthResponse.Token("access", "refresh");

        givenValidOnboardingToken("token");
        given(termAgreementValidator.validateAndGetAgreedTerms(request.termAgreements()))
                .willReturn(agreedTerms);
        given(memberRegistrationService.saveMember(any(Member.class))).willReturn(savedMember);
        given(authService.issueTokens(savedMember)).willReturn(token);

        AuthResponse.Token response = oAuthService.completeOnboarding("token", request);

        assertThat(response).isEqualTo(token);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRegistrationService).saveMember(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(memberCaptor.getValue().getNickname()).isEqualTo("tester");
        assertThat(memberCaptor.getValue().getProfileImageUrl())
                .isEqualTo("https://example.com/profile.jpg");

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
    void completeOnboardingRejectsDuplicateNickname() {
        OAuthRequest.Onboarding request = onboardingRequest("tester");
        givenValidOnboardingToken("token");
        willThrow(new MemberException(MemberErrorCode.ALREADY_REGISTERED_NICKNAME))
                .given(memberDuplicateValidator)
                .validateNicknameAvailable("tester");

        assertThatThrownBy(() -> oAuthService.completeOnboarding("token", request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_NICKNAME);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void completeOnboardingRejectsDuplicateOAuthAccount() {
        OAuthRequest.Onboarding request = onboardingRequest("tester");
        givenValidOnboardingToken("token");
        willThrow(new MemberException(MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT))
                .given(memberDuplicateValidator)
                .validateOAuthAccountAvailable(OAuthProvider.GOOGLE, "provider-user-id");

        assertThatThrownBy(() -> oAuthService.completeOnboarding("token", request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void completeOnboardingRejectsDuplicateEmail() {
        OAuthRequest.Onboarding request = onboardingRequest("tester");
        givenValidOnboardingToken("token");
        willThrow(new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL))
                .given(memberDuplicateValidator)
                .validateEmailAvailable("user@example.com");

        assertThatThrownBy(() -> oAuthService.completeOnboarding("token", request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_EMAIL);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    private void givenValidOnboardingToken(String token) {
        given(authService.validateOAuthOnboardingToken(token))
                .willReturn(new AuthService.OAuthOnboardingTokenClaims(
                        OAuthProvider.GOOGLE,
                        "provider-user-id",
                        " User@Example.com ",
                        "https://example.com/profile.jpg"
                ));
    }

    private OAuthRequest.Onboarding onboardingRequest(String nickname) {
        return new OAuthRequest.Onboarding(
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

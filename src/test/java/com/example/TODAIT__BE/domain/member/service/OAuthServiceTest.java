package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserClient;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserInfo;
import com.example.TODAIT__BE.domain.member.service.validator.MemberDuplicateValidator;
import com.example.TODAIT__BE.domain.member.service.validator.MemberLoginValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
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
                memberDuplicateValidator
        );
    }

    @Test
    void loginWithKakaoRequiresOnboardingWithNormalizedEmail() {
        given(kakaoOAuthUserClient.getUserInfo("kakao-token"))
                .willReturn(new OAuthUserInfo("provider-user-id", " User@Example.com "));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.empty());
        given(authService.issueOAuthOnboardingToken(
                OAuthProvider.KAKAO,
                "provider-user-id",
                "user@example.com"
        )).willReturn("onboarding-token");

        OAuthResponse.Login response = oAuthService.loginWithKakao("kakao-token");

        assertThat(response.loginStatus()).isEqualTo("ONBOARDING_REQUIRED");
        assertThat(response.onboardingToken()).isEqualTo("onboarding-token");
        assertThat(response.email()).isEqualTo("user@example.com");
        verify(memberDuplicateValidator).validateEmailAvailable("user@example.com");
    }

    @Test
    void loginWithKakaoLogsInExistingActiveMember() {
        Member member = Member.builder()
                .id(1L)
                .email("member@example.com")
                .nickname("member")
                .status(MemberStatus.ACTIVE)
                .build();
        MemberOAuthAccount account = MemberOAuthAccount.builder()
                .member(member)
                .provider(OAuthProvider.KAKAO)
                .providerUserId("provider-user-id")
                .build();
        AuthResponse.Token token = new AuthResponse.Token("access", "refresh");

        given(kakaoOAuthUserClient.getUserInfo("kakao-token"))
                .willReturn(new OAuthUserInfo("provider-user-id", "member@example.com"));
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
                .willReturn(new OAuthUserInfo("provider-user-id", "member@example.com"));
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
                .willReturn(new OAuthUserInfo("provider-user-id", "User@Example.com"));
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
                memberDuplicateValidator
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
                memberDuplicateValidator
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OAuth client is not configured");
    }
}

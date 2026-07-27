package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthLoginResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.infra.oauth.GoogleOAuthClient;
import com.example.TODAIT__BE.infra.oauth.KakaoOAuthClient;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

    @Mock
    private MemberOAuthAccountRepository memberOAuthAccountRepository;
    @Mock
    private AuthService authService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private KakaoOAuthClient kakaoOAuthClient;
    @Mock
    private GoogleOAuthClient googleOAuthClient;

    private OAuthService oAuthService;

    @BeforeEach
    void setUp() {
        oAuthService = new OAuthService(
                memberOAuthAccountRepository,
                authService,
                memberRepository,
                kakaoOAuthClient,
                googleOAuthClient
        );
    }

    @Test
    void loginWithKakaoRequiresOnboardingWithNormalizedEmail() {
        given(kakaoOAuthClient.getUserInfo("kakao-token"))
                .willReturn(new KakaoUserInfo("provider-user-id", " User@Example.com "));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.empty());
        given(memberRepository.existsByEmail("user@example.com")).willReturn(false);
        given(authService.issueOAuthOnboardingToken(
                OAuthProvider.KAKAO,
                "provider-user-id",
                "user@example.com"
        )).willReturn("onboarding-token");

        OAuthLoginResponse.OAuthLogin response = oAuthService.loginWithKakao("kakao-token");

        assertThat(response.loginStatus()).isEqualTo("ONBOARDING_REQUIRED");
        assertThat(response.onboardingToken()).isEqualTo("onboarding-token");
        assertThat(response.email()).isEqualTo("user@example.com");
        verify(memberRepository).existsByEmail("user@example.com");
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
        AuthTokenResponse.Token token = new AuthTokenResponse.Token("access", "refresh");

        given(kakaoOAuthClient.getUserInfo("kakao-token"))
                .willReturn(new KakaoUserInfo("provider-user-id", "member@example.com"));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.of(account));
        given(authService.issueTokens(member)).willReturn(token);

        OAuthLoginResponse.OAuthLogin response = oAuthService.loginWithKakao("kakao-token");

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

        given(kakaoOAuthClient.getUserInfo("kakao-token"))
                .willReturn(new KakaoUserInfo("provider-user-id", "member@example.com"));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.of(account));

        assertThatThrownBy(() -> oAuthService.loginWithKakao("kakao-token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_MEMBER_STATUS);
    }

    @Test
    void loginWithKakaoRejectsEmailAlreadyRegisteredByEmailSignup() {
        given(kakaoOAuthClient.getUserInfo("kakao-token"))
                .willReturn(new KakaoUserInfo("provider-user-id", "User@Example.com"));
        given(memberOAuthAccountRepository.findByProviderAndProviderUserId(
                OAuthProvider.KAKAO,
                "provider-user-id"
        )).willReturn(Optional.empty());
        given(memberRepository.existsByEmail("user@example.com")).willReturn(true);

        assertThatThrownBy(() -> oAuthService.loginWithKakao("kakao-token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_EMAIL);
    }
}

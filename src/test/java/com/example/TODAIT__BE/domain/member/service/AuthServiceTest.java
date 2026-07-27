package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.repository.RefreshTokenRepository;
import com.example.TODAIT__BE.global.security.JwtTokenProvider;
import com.example.TODAIT__BE.global.security.RefreshTokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RefreshTokenHasher refreshTokenHasher;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                jwtTokenProvider,
                refreshTokenRepository,
                memberRepository,
                refreshTokenHasher
        );
    }

    @Test
    void validateOAuthOnboardingTokenReturnsClaims() {
        given(jwtTokenProvider.validateToken("token")).willReturn(true);
        given(jwtTokenProvider.isOAuthOnboardingToken("token")).willReturn(true);
        given(jwtTokenProvider.getOAuthProvider("token")).willReturn(OAuthProvider.GOOGLE);
        given(jwtTokenProvider.getSubject("token")).willReturn("provider-user-id");
        given(jwtTokenProvider.getEmail("token")).willReturn("user@example.com");

        AuthService.OAuthOnboardingTokenClaims claims =
                authService.validateOAuthOnboardingToken("token");

        assertThat(claims.provider()).isEqualTo(OAuthProvider.GOOGLE);
        assertThat(claims.providerUserId()).isEqualTo("provider-user-id");
        assertThat(claims.email()).isEqualTo("user@example.com");
    }

    @Test
    void validateOAuthOnboardingTokenRejectsInvalidToken() {
        given(jwtTokenProvider.validateToken("token")).willReturn(false);

        assertThatThrownBy(() -> authService.validateOAuthOnboardingToken("token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_ONBOARDING_TOKEN);
    }

    @Test
    void validateOAuthOnboardingTokenRejectsNonOnboardingToken() {
        given(jwtTokenProvider.validateToken("token")).willReturn(true);
        given(jwtTokenProvider.isOAuthOnboardingToken("token")).willReturn(false);

        assertThatThrownBy(() -> authService.validateOAuthOnboardingToken("token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_ONBOARDING_TOKEN);
    }
}

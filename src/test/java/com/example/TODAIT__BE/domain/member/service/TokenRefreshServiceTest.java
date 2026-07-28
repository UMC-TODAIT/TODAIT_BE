package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.TokenRefreshRequest;
import com.example.TODAIT__BE.domain.member.dto.response.TokenRefreshResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.exception.AuthException;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.global.security.token.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.activeMember;
import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.member;
import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.refreshToken;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenRefreshServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenValidator refreshTokenValidator;
    @Mock
    private MemberLoginValidator memberLoginValidator;

    private TokenRefreshService tokenRefreshService;

    @BeforeEach
    void setUp() {
        tokenRefreshService = new TokenRefreshService(
                jwtTokenProvider,
                refreshTokenValidator,
                memberLoginValidator
        );
    }

    @Test
    void refreshIssuesNewAccessTokenForValidRefreshToken() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("refresh-token");
        Member member = activeMember(1L);
        RefreshToken storedToken = refreshToken(member, "refresh-token-hash", LocalDateTime.now().plusHours(1));

        given(refreshTokenValidator.validateAndGetStoredToken(request.refreshToken()))
                .willReturn(storedToken);
        given(jwtTokenProvider.createAccessToken(member)).willReturn("new-access-token");

        TokenRefreshResponse.AccessToken response = tokenRefreshService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        verify(memberLoginValidator).validateLoginAvailable(member);
    }

    @Test
    void refreshPropagatesRefreshTokenValidationFailure() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("invalid-refresh-token");

        given(refreshTokenValidator.validateAndGetStoredToken(request.refreshToken()))
                .willThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        assertThatThrownBy(() -> tokenRefreshService.refresh(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshRejectsInactiveMember() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("refresh-token");
        Member blockedMember = member(1L, "blocked", MemberStatus.BLOCKED);
        RefreshToken storedToken = refreshToken(blockedMember, "refresh-token-hash", LocalDateTime.now().plusHours(1));

        given(refreshTokenValidator.validateAndGetStoredToken(request.refreshToken()))
                .willReturn(storedToken);
        willThrow(new MemberException(MemberErrorCode.INVALID_MEMBER_STATUS))
                .given(memberLoginValidator)
                .validateLoginAvailable(blockedMember);

        assertThatThrownBy(() -> tokenRefreshService.refresh(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_MEMBER_STATUS);
    }
}

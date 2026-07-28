package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.LogoutRequest;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.exception.AuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private RefreshTokenValidator refreshTokenValidator;

    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(refreshTokenValidator);
    }

    @Test
    void logoutRevokesValidRefreshToken() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().plusHours(1));

        given(refreshTokenValidator.validateAndGetStoredToken(request.refreshToken()))
                .willReturn(storedToken);

        logoutService.logout(request);

        assertThat(storedToken.isRevoked()).isTrue();
    }

    @Test
    void logoutPropagatesRefreshTokenValidationFailure() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("invalid-refresh-token");

        given(refreshTokenValidator.validateAndGetStoredToken(request.refreshToken()))
                .willThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        assertThatThrownBy(() -> logoutService.logout(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    private Member activeMember(Long id) {
        return Member.builder()
                .id(id)
                .nickname("member-" + id)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    private RefreshToken refreshToken(
            Member member,
            String tokenHash,
            LocalDateTime expiresAt
    ) {
        return RefreshToken.builder()
                .member(member)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();
    }
}

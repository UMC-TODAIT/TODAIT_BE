package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.exception.AuthException;
import com.example.TODAIT__BE.domain.member.service.validator.RefreshTokenValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.activeMember;
import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.refreshToken;
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
        AuthRequest.Logout request = new AuthRequest.Logout("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().plusHours(1));

        given(refreshTokenValidator.validateAndGetStoredToken(request.refreshToken()))
                .willReturn(storedToken);

        logoutService.logout(request);

        assertThat(storedToken.isRevoked()).isTrue();
    }

    @Test
    void logoutPropagatesRefreshTokenValidationFailure() {
        AuthRequest.Logout request = new AuthRequest.Logout("invalid-refresh-token");

        given(refreshTokenValidator.validateAndGetStoredToken(request.refreshToken()))
                .willThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        assertThatThrownBy(() -> logoutService.logout(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}

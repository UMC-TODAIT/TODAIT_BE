package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.LogoutRequest;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.exception.AuthException;
import com.example.TODAIT__BE.domain.member.repository.RefreshTokenRepository;
import com.example.TODAIT__BE.global.security.token.JwtTokenProvider;
import com.example.TODAIT__BE.global.security.token.RefreshTokenHasher;
import com.example.TODAIT__BE.global.security.token.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenHasher refreshTokenHasher;

    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(
                jwtTokenProvider,
                refreshTokenRepository,
                refreshTokenHasher
        );
    }

    @Test
    void logoutRevokesValidRefreshToken() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().plusHours(1));

        givenStoredRefreshToken(request.refreshToken(), 1L, storedToken);

        logoutService.logout(request);

        assertThat(storedToken.isRevoked()).isTrue();
    }

    @Test
    void logoutRejectsNonRefreshTokenType() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("access-token");
        given(jwtTokenProvider.getTokenType("access-token")).willReturn(TokenType.ACCESS);

        assertThatThrownBy(() -> logoutService.logout(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void logoutRejectsExpiredJwtRefreshToken() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("expired-refresh-token");
        willThrow(new ExpiredJwtException(null, null, "expired"))
                .given(jwtTokenProvider)
                .getTokenType("expired-refresh-token");

        assertThatThrownBy(() -> logoutService.logout(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void logoutRejectsMalformedJwtRefreshToken() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("malformed-refresh-token");
        willThrow(new JwtException("malformed"))
                .given(jwtTokenProvider)
                .getTokenType("malformed-refresh-token");

        assertThatThrownBy(() -> logoutService.logout(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void logoutRejectsUnknownStoredToken() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("refresh-token");

        givenValidRefreshJwt("refresh-token", 1L);
        given(refreshTokenHasher.hash("refresh-token")).willReturn("refresh-token-hash");
        given(refreshTokenRepository.findByTokenHash("refresh-token-hash"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> logoutService.logout(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void logoutRejectsRevokedStoredToken() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().plusHours(1));
        storedToken.revoke();

        givenStoredRefreshToken(request.refreshToken(), 1L, storedToken);

        assertThatThrownBy(() -> logoutService.logout(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REVOKED_REFRESH_TOKEN);
    }

    @Test
    void logoutRejectsExpiredStoredToken() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().minusSeconds(1));

        givenStoredRefreshToken(request.refreshToken(), 1L, storedToken);

        assertThatThrownBy(() -> logoutService.logout(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void logoutRejectsTokenWhoseSubjectDiffersFromStoredMember() {
        LogoutRequest.Logout request = new LogoutRequest.Logout("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(2L), "refresh-token-hash", LocalDateTime.now().plusHours(1));

        givenStoredRefreshToken(request.refreshToken(), 1L, storedToken);

        assertThatThrownBy(() -> logoutService.logout(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    private void givenStoredRefreshToken(
            String token,
            Long memberId,
            RefreshToken storedToken
    ) {
        givenValidRefreshJwt(token, memberId);
        given(refreshTokenHasher.hash(token)).willReturn("refresh-token-hash");
        given(refreshTokenRepository.findByTokenHash("refresh-token-hash"))
                .willReturn(Optional.of(storedToken));
    }

    private void givenValidRefreshJwt(String token, Long memberId) {
        given(jwtTokenProvider.getTokenType(token)).willReturn(TokenType.REFRESH);
        given(jwtTokenProvider.getMemberId(token)).willReturn(memberId);
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

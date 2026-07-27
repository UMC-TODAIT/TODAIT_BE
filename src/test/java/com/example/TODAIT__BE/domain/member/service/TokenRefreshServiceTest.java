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
import com.example.TODAIT__BE.domain.member.repository.RefreshTokenRepository;
import com.example.TODAIT__BE.global.security.token.JwtTokenProvider;
import com.example.TODAIT__BE.global.security.token.RefreshTokenHasher;
import io.jsonwebtoken.ExpiredJwtException;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenRefreshServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenHasher refreshTokenHasher;
    @Mock
    private MemberLoginValidator memberLoginValidator;

    private TokenRefreshService tokenRefreshService;

    @BeforeEach
    void setUp() {
        tokenRefreshService = new TokenRefreshService(
                jwtTokenProvider,
                refreshTokenRepository,
                refreshTokenHasher,
                memberLoginValidator
        );
    }

    @Test
    void refreshIssuesNewAccessTokenForValidRefreshToken() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("refresh-token");
        Member member = activeMember(1L);
        RefreshToken storedToken = refreshToken(member, "refresh-token-hash", LocalDateTime.now().plusHours(1));

        givenValidRefreshJwt("refresh-token", 1L);
        given(refreshTokenHasher.hash("refresh-token")).willReturn("refresh-token-hash");
        given(refreshTokenRepository.findByTokenHash("refresh-token-hash"))
                .willReturn(Optional.of(storedToken));
        given(jwtTokenProvider.createAccessToken(member)).willReturn("new-access-token");

        TokenRefreshResponse.AccessToken response = tokenRefreshService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        verify(memberLoginValidator).validateLoginAvailable(member);
    }

    @Test
    void refreshRejectsNonRefreshTokenType() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("access-token");
        given(jwtTokenProvider.getTokenType("access-token")).willReturn("ACCESS");

        assertThatThrownBy(() -> tokenRefreshService.refresh(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshRejectsExpiredJwtRefreshToken() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("expired-refresh-token");
        willThrow(new ExpiredJwtException(null, null, "expired"))
                .given(jwtTokenProvider)
                .getTokenType("expired-refresh-token");

        assertThatThrownBy(() -> tokenRefreshService.refresh(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void refreshRejectsUnknownStoredToken() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("refresh-token");

        givenValidRefreshJwt("refresh-token", 1L);
        given(refreshTokenHasher.hash("refresh-token")).willReturn("refresh-token-hash");
        given(refreshTokenRepository.findByTokenHash("refresh-token-hash"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> tokenRefreshService.refresh(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshRejectsRevokedStoredToken() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().plusHours(1));
        storedToken.revoke();

        givenStoredRefreshToken(request.refreshToken(), 1L, storedToken);

        assertThatThrownBy(() -> tokenRefreshService.refresh(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REVOKED_REFRESH_TOKEN);
    }

    @Test
    void refreshRejectsExpiredStoredToken() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().minusSeconds(1));

        givenStoredRefreshToken(request.refreshToken(), 1L, storedToken);

        assertThatThrownBy(() -> tokenRefreshService.refresh(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void refreshRejectsTokenWhoseSubjectDiffersFromStoredMember() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(2L), "refresh-token-hash", LocalDateTime.now().plusHours(1));

        givenStoredRefreshToken(request.refreshToken(), 1L, storedToken);

        assertThatThrownBy(() -> tokenRefreshService.refresh(request))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshRejectsInactiveMember() {
        TokenRefreshRequest.Refresh request = new TokenRefreshRequest.Refresh("refresh-token");
        Member blockedMember = Member.builder()
                .id(1L)
                .nickname("blocked")
                .status(MemberStatus.BLOCKED)
                .build();
        RefreshToken storedToken = refreshToken(blockedMember, "refresh-token-hash", LocalDateTime.now().plusHours(1));

        givenStoredRefreshToken(request.refreshToken(), 1L, storedToken);
        willThrow(new MemberException(MemberErrorCode.INVALID_MEMBER_STATUS))
                .given(memberLoginValidator)
                .validateLoginAvailable(blockedMember);

        assertThatThrownBy(() -> tokenRefreshService.refresh(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_MEMBER_STATUS);
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
        given(jwtTokenProvider.getTokenType(token)).willReturn("REFRESH");
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

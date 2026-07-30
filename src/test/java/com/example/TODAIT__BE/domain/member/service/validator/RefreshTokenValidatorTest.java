package com.example.TODAIT__BE.domain.member.service.validator;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
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
import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.activeMember;
import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.refreshToken;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class RefreshTokenValidatorTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenHasher refreshTokenHasher;

    private RefreshTokenValidator refreshTokenValidator;

    @BeforeEach
    void setUp() {
        refreshTokenValidator = new RefreshTokenValidator(
                jwtTokenProvider,
                refreshTokenRepository,
                refreshTokenHasher
        );
    }

    @Test
    void validateAndGetStoredTokenReturnsValidRefreshToken() {
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().plusHours(1));

        givenStoredRefreshToken("refresh-token", 1L, storedToken);

        RefreshToken result = refreshTokenValidator.validateAndGetStoredToken("refresh-token");

        assertThat(result).isSameAs(storedToken);
    }

    @Test
    void validateAndGetStoredTokenRejectsNonRefreshTokenType() {
        given(jwtTokenProvider.getTokenType("access-token")).willReturn(TokenType.ACCESS);

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("access-token"))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsExpiredJwtRefreshToken() {
        willThrow(new ExpiredJwtException(null, null, "expired"))
                .given(jwtTokenProvider)
                .getTokenType("expired-refresh-token");

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("expired-refresh-token"))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsMalformedJwtRefreshToken() {
        willThrow(new JwtException("malformed"))
                .given(jwtTokenProvider)
                .getTokenType("malformed-refresh-token");

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("malformed-refresh-token"))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsUnknownStoredToken() {
        givenValidRefreshJwt("refresh-token", 1L);
        given(refreshTokenHasher.hash("refresh-token")).willReturn("refresh-token-hash");
        given(refreshTokenRepository.findByTokenHash("refresh-token-hash"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("refresh-token"))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsTokenWhoseSubjectDiffersFromStoredMemberBeforeStateChecks() {
        RefreshToken storedToken = refreshToken(activeMember(2L), "refresh-token-hash", LocalDateTime.now().plusHours(1));
        storedToken.revoke();

        givenStoredRefreshToken("refresh-token", 1L, storedToken);

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("refresh-token"))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsRevokedStoredToken() {
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().plusHours(1));
        storedToken.revoke();

        givenStoredRefreshToken("refresh-token", 1L, storedToken);

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("refresh-token"))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REVOKED_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsExpiredStoredToken() {
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().minusSeconds(1));

        givenStoredRefreshToken("refresh-token", 1L, storedToken);

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("refresh-token"))
                .isInstanceOf(AuthException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
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
}

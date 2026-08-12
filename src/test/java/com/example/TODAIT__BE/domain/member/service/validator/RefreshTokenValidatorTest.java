package com.example.TODAIT__BE.domain.member.service.validator;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.RefreshTokenRepository;
import com.example.TODAIT__BE.global.security.token.JwtTokenProvider;
import com.example.TODAIT__BE.global.security.token.JwtTokenProvider.ParsedTokenClaims;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    void validateAndGetStoredTokenForUpdateUsesLockedRepositoryQuery() {
        RefreshToken storedToken = refreshToken(
                activeMember(1L),
                "refresh-token-hash",
                LocalDateTime.now().plusHours(1)
        );

        givenValidRefreshJwt("refresh-token", 1L);
        given(refreshTokenHasher.hash("refresh-token"))
                .willReturn("refresh-token-hash");
        given(refreshTokenRepository.findByTokenHashForUpdate("refresh-token-hash"))
                .willReturn(Optional.of(storedToken));

        RefreshToken result = refreshTokenValidator
                .validateAndGetStoredTokenForUpdate("refresh-token");

        assertThat(result).isSameAs(storedToken);
    }

    @Test
    void validateAndGetStoredTokenForUpdateRejectsRevokedToken() {
        RefreshToken storedToken = refreshToken(
                activeMember(1L),
                "refresh-token-hash",
                LocalDateTime.now().plusHours(1)
        );
        storedToken.revoke();

        givenValidRefreshJwt("refresh-token", 1L);
        given(refreshTokenHasher.hash("refresh-token"))
                .willReturn("refresh-token-hash");
        given(refreshTokenRepository.findByTokenHashForUpdate("refresh-token-hash"))
                .willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenValidator
                .validateAndGetStoredTokenForUpdate("refresh-token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REVOKED_REFRESH_TOKEN);
    }

    @Test
    void validatedRefreshTokenCanBeReusedWithoutRevalidatingJwt() {
        RefreshToken storedToken = refreshToken(
                activeMember(1L),
                "refresh-token-hash",
                LocalDateTime.now().plusHours(1)
        );
        givenValidRefreshJwt("refresh-token", 1L);
        given(refreshTokenHasher.hash("refresh-token"))
                .willReturn("refresh-token-hash");
        given(refreshTokenRepository.findByTokenHashForUpdate("refresh-token-hash"))
                .willReturn(Optional.of(storedToken));

        RefreshTokenValidator.ValidatedRefreshToken validatedToken =
                refreshTokenValidator.validate("refresh-token");
        RefreshToken result = refreshTokenValidator
                .validateAndGetStoredTokenForUpdate(validatedToken);

        assertThat(result).isSameAs(storedToken);
        verify(jwtTokenProvider, times(1)).parseTokenClaims("refresh-token");
    }

    @Test
    void validateAndGetStoredTokenRejectsNonRefreshTokenType() {
        given(jwtTokenProvider.parseTokenClaims("access-token"))
                .willReturn(new ParsedTokenClaims(TokenType.ACCESS, "1"));

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("access-token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsExpiredJwtRefreshToken() {
        willThrow(new ExpiredJwtException(null, null, "expired"))
                .given(jwtTokenProvider)
                .parseTokenClaims("expired-refresh-token");

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("expired-refresh-token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsMalformedJwtRefreshToken() {
        willThrow(new JwtException("malformed"))
                .given(jwtTokenProvider)
                .parseTokenClaims("malformed-refresh-token");

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("malformed-refresh-token"))
                .isInstanceOf(MemberException.class)
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
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsTokenWhoseSubjectDiffersFromStoredMemberBeforeStateChecks() {
        RefreshToken storedToken = refreshToken(activeMember(2L), "refresh-token-hash", LocalDateTime.now().plusHours(1));
        storedToken.revoke();

        givenStoredRefreshToken("refresh-token", 1L, storedToken);

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("refresh-token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsRevokedStoredToken() {
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().plusHours(1));
        storedToken.revoke();

        givenStoredRefreshToken("refresh-token", 1L, storedToken);

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("refresh-token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.REVOKED_REFRESH_TOKEN);
    }

    @Test
    void validateAndGetStoredTokenRejectsExpiredStoredToken() {
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().minusSeconds(1));

        givenStoredRefreshToken("refresh-token", 1L, storedToken);

        assertThatThrownBy(() -> refreshTokenValidator.validateAndGetStoredToken("refresh-token"))
                .isInstanceOf(MemberException.class)
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
        given(jwtTokenProvider.parseTokenClaims(token))
                .willReturn(new ParsedTokenClaims(
                        TokenType.REFRESH,
                        String.valueOf(memberId)
                ));
    }
}

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RefreshTokenValidator {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;

    public RefreshToken validateAndGetStoredToken(String token) {
        ValidatedRefreshToken validatedToken = validate(token);
        return validateAndGetStoredToken(validatedToken, false);
    }

    public RefreshToken validateAndGetStoredTokenForUpdate(String token) {
        ValidatedRefreshToken validatedToken = validate(token);
        return validateAndGetStoredToken(validatedToken, true);
    }

    public RefreshToken validateAndGetStoredTokenForUpdate(
            ValidatedRefreshToken validatedToken
    ) {
        return validateAndGetStoredToken(validatedToken, true);
    }

    private RefreshToken validateAndGetStoredToken(
            ValidatedRefreshToken validatedToken,
            boolean lockForUpdate
    ) {
        String tokenHash = refreshTokenHasher.hash(validatedToken.token());

        RefreshToken storedToken = (lockForUpdate
                ? refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                : refreshTokenRepository.findByTokenHash(tokenHash))
                .orElseThrow(() -> new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedToken.getMember().getId().equals(validatedToken.memberId())) {
            throw new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (storedToken.isRevoked()) {
            throw new MemberException(AuthErrorCode.REVOKED_REFRESH_TOKEN);
        }

        if (!storedToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new MemberException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        return storedToken;
    }

    public ValidatedRefreshToken validate(String token) {
        try {
            ParsedTokenClaims claims = jwtTokenProvider.parseTokenClaims(token);

            if (TokenType.REFRESH != claims.tokenType()) {
                throw new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN);
            }
            return new ValidatedRefreshToken(
                    token,
                    Long.valueOf(claims.subject())
            );
        } catch (ExpiredJwtException e) {
            throw new MemberException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    public static final class ValidatedRefreshToken {

        private final String token;
        private final Long memberId;

        private ValidatedRefreshToken(String token, Long memberId) {
            this.token = token;
            this.memberId = memberId;
        }

        private String token() {
            return token;
        }

        public Long memberId() {
            return memberId;
        }
    }
}

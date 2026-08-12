package com.example.TODAIT__BE.domain.member.service.validator;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.RefreshTokenRepository;
import com.example.TODAIT__BE.global.security.token.JwtTokenProvider;
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
        Long memberId = validateAndExtractMemberId(token);
        return validateAndGetStoredToken(token, memberId, false);
    }

    public RefreshToken validateAndGetStoredTokenForUpdate(String token) {
        Long memberId = validateAndExtractMemberId(token);
        return validateAndGetStoredToken(token, memberId, true);
    }

    public RefreshToken validateAndGetStoredTokenForUpdate(
            String token,
            Long expectedMemberId
    ) {
        Long tokenMemberId = validateAndExtractMemberId(token);

        if (!tokenMemberId.equals(expectedMemberId)) {
            throw new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        return validateAndGetStoredToken(token, tokenMemberId, true);
    }

    private RefreshToken validateAndGetStoredToken(
            String token,
            Long expectedMemberId,
            boolean lockForUpdate
    ) {
        String tokenHash = refreshTokenHasher.hash(token);

        RefreshToken storedToken = (lockForUpdate
                ? refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                : refreshTokenRepository.findByTokenHash(tokenHash))
                .orElseThrow(() -> new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (!storedToken.getMember().getId().equals(expectedMemberId)) {
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

    public Long validateAndExtractMemberId(String token) {
        try {
            if (TokenType.REFRESH != jwtTokenProvider.getTokenType(token)) {
                throw new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN);
            }
            return jwtTokenProvider.getMemberId(token);
        } catch (ExpiredJwtException e) {
            throw new MemberException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}

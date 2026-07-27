package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.TokenRefreshRequest;
import com.example.TODAIT__BE.domain.member.dto.response.TokenRefreshResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.exception.AuthException;
import com.example.TODAIT__BE.domain.member.repository.RefreshTokenRepository;
import com.example.TODAIT__BE.global.security.token.JwtTokenProvider;
import com.example.TODAIT__BE.global.security.token.RefreshTokenHasher;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;
    private final MemberLoginValidator memberLoginValidator;

    @Transactional(readOnly = true)
    public TokenRefreshResponse.AccessToken refresh(
            TokenRefreshRequest.Refresh request
    ){
        String token = request.refreshToken();

        Long memberId = validateAndExtractMemberId(token);

        String tokenHash = refreshTokenHasher.hash(token);

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow( ()-> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if(storedToken.isRevoked()){
            throw new AuthException(AuthErrorCode.REVOKED_REFRESH_TOKEN);
        }

        if(!storedToken.getExpiresAt().isAfter(LocalDateTime.now())){
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        Member member = storedToken.getMember();

        if(!member.getId().equals(memberId)){
            throw new AuthException(
                    AuthErrorCode.INVALID_REFRESH_TOKEN
            );
        }

        memberLoginValidator.validateLoginAvailable(member);

        String newAccessToken = jwtTokenProvider.createAccessToken(member);

        return TokenRefreshResponse.AccessToken.builder()
                .accessToken(newAccessToken)
                .build();
    }


    private Long validateAndExtractMemberId(String token){
        try{
            if(!"REFRESH".equals(jwtTokenProvider.getTokenType(token)
            )){
                throw new AuthException(
                        AuthErrorCode.INVALID_REFRESH_TOKEN
                );
            }
            return jwtTokenProvider.getMemberId(token);
        }catch (ExpiredJwtException e){
            throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
        }catch(JwtException | IllegalArgumentException e){
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}

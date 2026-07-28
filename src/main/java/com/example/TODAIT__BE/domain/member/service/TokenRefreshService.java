package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.dto.request.TokenRefreshRequest;
import com.example.TODAIT__BE.domain.member.dto.response.TokenRefreshResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.global.security.token.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenRefreshService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenValidator refreshTokenValidator;
    private final MemberLoginValidator memberLoginValidator;

    @Transactional(readOnly = true)
    public TokenRefreshResponse.AccessToken refresh(
            TokenRefreshRequest.Refresh request
    ){
        RefreshToken storedToken = refreshTokenValidator.validateAndGetStoredToken(request.refreshToken());
        Member member = storedToken.getMember();

        memberLoginValidator.validateLoginAvailable(member);

        String newAccessToken = jwtTokenProvider.createAccessToken(member);

        return TokenRefreshResponse.AccessToken.builder()
                .accessToken(newAccessToken)
                .build();
    }
}

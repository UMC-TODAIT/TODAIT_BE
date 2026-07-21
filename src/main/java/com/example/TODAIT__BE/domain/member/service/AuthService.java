package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.repository.RefreshTokenRepository;
import com.example.TODAIT__BE.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public AuthTokenResponse.Token issueTokens(Member member){

        Member managedMember = memberRepository.findByIdForUpdate(member.getId())
                .orElseThrow(() -> new IllegalStateException("토큰 발급 대상 회원을 찾을 수 없습니다."));

        LocalDateTime issuedAt = LocalDateTime.now();

        managedMember.updateLastLoginAt(issuedAt);

        String accessToken = jwtTokenProvider.createAccessToken(managedMember);
        String refreshToken = jwtTokenProvider.createRefreshToken(managedMember);
        String refreshTokenHash = hashToken(refreshToken);

        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByMemberAndRevokedAtIsNull(managedMember);
        activeTokens.forEach(RefreshToken::revoke);

        LocalDateTime expiresAt = issuedAt.plus(
                Duration.ofMillis(
                        jwtTokenProvider.getRefreshTokenExpiration()
                )
        );

        RefreshToken newRefreshToken = RefreshToken.builder()
                .member(managedMember)
                .tokenHash(refreshTokenHash)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return AuthTokenResponse.Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String issueOAuthOnboardingToken(
            OAuthProvider provider,
            String providerUserId,
            String email
    ) {
        return jwtTokenProvider.createOAuthOnboardingToken(
                provider,
                providerUserId,
                email
        );
    }

    private String hashToken(String token){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("토큰 해시 생성에 실패했습니다.", e);
        }
    }


}

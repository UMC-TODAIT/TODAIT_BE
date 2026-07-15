package com.example.TODAIT__BE.global.security;

import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private static final int MIN_SECRET_KEY_BYTES = 32;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.onboarding-token-expiration}")
    private long onboardingTokenExpiration;

    @PostConstruct
    private void validateSecretKey() {
        int secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8).length;
        if (secretKeyBytes < MIN_SECRET_KEY_BYTES) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes.");
        }
    }

    public String createAccessToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType","ACCESS");
        claims.put("role",member.getRole().name());

        return createToken(
                String.valueOf(member.getId()),
                claims,
                accessTokenExpiration
        );
    }

    public String createRefreshToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType","REFRESH");

        return createToken(
                String.valueOf(member.getId()),
                claims,
                refreshTokenExpiration
        );
    }

    public String createOAuthOnboardingToken(
            OAuthProvider provider,
            String providerUserId,
            String email
    ) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType","OAUTH_ONBOARDING");
        claims.put("provider", provider.name());
        claims.put("email", email);

        return createToken(
               providerUserId,
                claims,
                onboardingTokenExpiration
        );
    }

    private String createToken(
            String subject,
            Map<String, Object> claims,
            long expirationMillis
    ) {
        Date now = new Date();
        Date expiration = new Date(now.getTime()+expirationMillis);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    public long getRefreshTokenExpiration(){
        return refreshTokenExpiration;
    }

    //공통으로 claims 꺼내기
    private Claims parseClaims(String token){
        SecretKey key = getSigningKey();

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token){
        try{
            parseClaims(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public String getSubject(String token){
        return parseClaims(token).getSubject();
    }

    public String getTokenType(String token){
        return parseClaims(token).get("tokenType",String.class);
    }

    public Long getMemberId(String token) {
        return Long.valueOf(getSubject(token));
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public OAuthProvider getOAuthProvider(String token){
        String provider = parseClaims(token).get("provider",String.class);
        return OAuthProvider.valueOf(provider);
    }

    public String getEmail(String token){
        return parseClaims(token).get("email", String.class);
    }

    public boolean isOAuthOnboardingToken(String token){
        return "OAUTH_ONBOARDING".equals(getTokenType(token));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }


}


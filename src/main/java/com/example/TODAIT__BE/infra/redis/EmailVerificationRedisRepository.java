package com.example.TODAIT__BE.infra.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Repository
public class EmailVerificationRedisRepository {

    private static final String CODE_KEY_PREFIX = "email-verification:code:";
    private static final String VERIFIED_KEY_PREFIX = "email-verification:verified:";
    private static final String VERIFIED_VALUE = "true";

    private final RedisTemplate<String, String> redisTemplate;
    private final Duration codeTtl;
    private final Duration verifiedTtl;

    public EmailVerificationRedisRepository(
            RedisTemplate<String, String> redisTemplate,
            @Value("${app.email-verification.code-ttl-minutes}") long codeTtlMinutes,
            @Value("${app.email-verification.verified-ttl-minutes:30}") long verifiedTtlMinutes
    ) {
        this.redisTemplate = redisTemplate;
        this.codeTtl = Duration.ofMinutes(codeTtlMinutes);
        this.verifiedTtl = Duration.ofMinutes(verifiedTtlMinutes);
    }

    public void saveCode(String email, String code) {
        redisTemplate.opsForValue()
                .set(codeKey(email), code, codeTtl);
    }

    public Optional<String> findCodeByEmail(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue()
                .get(codeKey(email)));
    }

    public void deleteCode(String email) {
        redisTemplate.delete(codeKey(email));
    }

    public void saveVerified(String email) {
        redisTemplate.opsForValue()
                .set(verifiedKey(email), VERIFIED_VALUE, verifiedTtl);
    }

    public boolean isVerified(String email) {
        return VERIFIED_VALUE.equals(redisTemplate.opsForValue()
                .get(verifiedKey(email)));
    }

    public void deleteVerified(String email) {
        redisTemplate.delete(verifiedKey(email));
    }

    private String codeKey(String email) {
        return CODE_KEY_PREFIX + normalizeEmail(email);
    }

    private String verifiedKey(String email) {
        return VERIFIED_KEY_PREFIX + normalizeEmail(email);
    }

    private String normalizeEmail(String email) {
        return email.trim()
                .toLowerCase(Locale.ROOT);
    }
}

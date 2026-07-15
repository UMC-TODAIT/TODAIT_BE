package com.example.TODAIT__BE.infra.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Repository
public class EmailVerificationRedisRepository {

    private static final String CODE_KEY_PREFIX = "email-verification:code:";
    private static final String VERIFIED_KEY_PREFIX = "email-verification:verified:";
    private static final String RESEND_COOLDOWN_KEY_PREFIX = "email-verification:resend-cooldown:";
    private static final String VERIFY_FAILURE_KEY_PREFIX = "email-verification:verify-failure:";
    private static final String VERIFIED_VALUE = "true";
    private static final String COOLDOWN_VALUE = "1";

    private static final DefaultRedisScript<Long> SAVE_CODE_IF_NOT_COOLING_DOWN_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('exists', KEYS[2]) == 1 then
                        return 0
                    end

                    redis.call('set', KEYS[1], ARGV[1], 'PX', ARGV[2])
                    redis.call('set', KEYS[2], ARGV[3], 'PX', ARGV[4])
                    redis.call('del', KEYS[3])
                    return 1
                    """, Long.class);

    private static final DefaultRedisScript<Long> DELETE_CODE_AND_COOLDOWN_IF_MATCHES_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        redis.call('del', KEYS[1])
                        redis.call('del', KEYS[2])
                        return 1
                    end

                    return 0
                    """, Long.class);

    private static final DefaultRedisScript<Long> VERIFY_CODE_AND_MARK_VERIFIED_SCRIPT =
            new DefaultRedisScript<>("""
                    local failureCount = tonumber(redis.call('get', KEYS[3]) or '0')
                    local maxFailures = tonumber(ARGV[4])

                    if failureCount >= maxFailures then
                        return 4
                    end

                    local savedCode = redis.call('get', KEYS[1])
                    if not savedCode then
                        return 0
                    end

                    if savedCode ~= ARGV[1] then
                        failureCount = redis.call('incr', KEYS[3])
                        if failureCount == 1 then
                            redis.call('pexpire', KEYS[3], ARGV[5])
                        end
                        if failureCount >= maxFailures then
                            return 4
                        end
                        return 1
                    end

                    redis.call('del', KEYS[1])
                    redis.call('del', KEYS[3])
                    redis.call('set', KEYS[2], ARGV[2], 'PX', ARGV[3])
                    return 2
                    """, Long.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final Duration codeTtl;
    private final Duration verifiedTtl;
    private final Duration resendCooldownTtl;
    private final Duration verifyFailureTtl;
    private final int maxVerifyFailures;

    public EmailVerificationRedisRepository(
            RedisTemplate<String, String> redisTemplate,
            @Value("${app.email-verification.code-ttl-minutes}") long codeTtlMinutes,
            @Value("${app.email-verification.verified-ttl-minutes:30}") long verifiedTtlMinutes,
            @Value("${app.email-verification.resend-cooldown-seconds:60}") long resendCooldownSeconds,
            @Value("${app.email-verification.verify-failure-ttl-minutes:5}") long verifyFailureTtlMinutes,
            @Value("${app.email-verification.max-verify-failures:5}") int maxVerifyFailures
    ) {
        this.redisTemplate = redisTemplate;
        this.codeTtl = Duration.ofMinutes(codeTtlMinutes);
        this.verifiedTtl = Duration.ofMinutes(verifiedTtlMinutes);
        this.resendCooldownTtl = Duration.ofSeconds(resendCooldownSeconds);
        this.verifyFailureTtl = Duration.ofMinutes(verifyFailureTtlMinutes);
        this.maxVerifyFailures = maxVerifyFailures;
    }

    public boolean saveCodeIfNotCoolingDown(String email, String code) {
        Long result = redisTemplate.execute(
                SAVE_CODE_IF_NOT_COOLING_DOWN_SCRIPT,
                List.of(codeKey(email), resendCooldownKey(email), verifyFailureKey(email)),
                code,
                String.valueOf(codeTtl.toMillis()),
                COOLDOWN_VALUE,
                String.valueOf(resendCooldownTtl.toMillis())
        );

        return Long.valueOf(1L).equals(result);
    }

    public void deleteCodeAndCooldownIfMatches(String email, String code) {
        redisTemplate.execute(
                DELETE_CODE_AND_COOLDOWN_IF_MATCHES_SCRIPT,
                List.of(codeKey(email), resendCooldownKey(email)),
                code
        );
    }

    public VerifyCodeResult verifyCodeAndMarkVerified(String email, String code) {
        Long result = redisTemplate.execute(
                VERIFY_CODE_AND_MARK_VERIFIED_SCRIPT,
                List.of(codeKey(email), verifiedKey(email), verifyFailureKey(email)),
                code,
                VERIFIED_VALUE,
                String.valueOf(verifiedTtl.toMillis()),
                String.valueOf(maxVerifyFailures),
                String.valueOf(verifyFailureTtl.toMillis())
        );

        return VerifyCodeResult.from(result);
    }

    public boolean isVerified(String email) {
        return VERIFIED_VALUE.equals(redisTemplate.opsForValue()
                .get(verifiedKey(email)));
    }

    private String codeKey(String email) {
        return CODE_KEY_PREFIX + normalizeEmail(email);
    }

    private String verifiedKey(String email) {
        return VERIFIED_KEY_PREFIX + normalizeEmail(email);
    }

    private String resendCooldownKey(String email) {
        return RESEND_COOLDOWN_KEY_PREFIX + normalizeEmail(email);
    }

    private String verifyFailureKey(String email) {
        return VERIFY_FAILURE_KEY_PREFIX + normalizeEmail(email);
    }

    private String normalizeEmail(String email) {
        return email.trim()
                .toLowerCase(Locale.ROOT);
    }

    public enum VerifyCodeResult {
        CODE_NOT_FOUND,
        CODE_MISMATCH,
        VERIFIED,
        VERIFY_ATTEMPT_EXCEEDED;

        private static VerifyCodeResult from(Long result) {
            if (Long.valueOf(2L).equals(result)) {
                return VERIFIED;
            }
            if (Long.valueOf(1L).equals(result)) {
                return CODE_MISMATCH;
            }
            if (Long.valueOf(4L).equals(result)) {
                return VERIFY_ATTEMPT_EXCEEDED;
            }

            return CODE_NOT_FOUND;
        }
    }
}

package com.example.TODAIT__BE.infra.redis;

import com.example.TODAIT__BE.domain.member.service.port.PasswordResetStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Repository
public class PasswordResetRedisRepository implements PasswordResetStore {

    private static final String CODE_KEY_PREFIX = "password-reset:code:";
    private static final String SESSION_KEY_PREFIX = "password-reset:session:";
    private static final String RESET_TOKEN_KEY_PREFIX = "password-reset:reset-token:";
    private static final String RESEND_COOLDOWN_KEY_PREFIX = "password-reset:resend-cooldown:";
    private static final String VERIFY_FAILURE_KEY_PREFIX = "password-reset:verify-failure:";
    private static final String SESSION_VALUE = "requested";
    private static final String COOLDOWN_VALUE = "1";

    private static final DefaultRedisScript<Long> SAVE_CODE_IF_NOT_COOLING_DOWN_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('exists', KEYS[3]) == 1 then
                        return 0
                    end

                    redis.call('set', KEYS[1], ARGV[1], 'PX', ARGV[2])
                    redis.call('set', KEYS[2], ARGV[3], 'PX', ARGV[4])
                    redis.call('set', KEYS[3], ARGV[5], 'PX', ARGV[6])
                    redis.call('del', KEYS[4])
                    return 1
                    """, Long.class);

    private static final DefaultRedisScript<Long> DELETE_CODE_AND_COOLDOWN_IF_MATCHES_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        redis.call('del', KEYS[1])
                        redis.call('del', KEYS[2])
                        redis.call('del', KEYS[3])
                        return 1
                    end

                    return 0
                    """, Long.class);

    private static final DefaultRedisScript<Long> VERIFY_CODE_AND_SAVE_RESET_TOKEN_SCRIPT =
            new DefaultRedisScript<>("""
                    local failureCount = tonumber(redis.call('get', KEYS[3]) or '0')
                    local maxFailures = tonumber(ARGV[4])

                    if failureCount >= maxFailures then
                        return 4
                    end

                    local savedCode = redis.call('get', KEYS[1])
                    if not savedCode then
                        if redis.call('exists', KEYS[2]) == 1 then
                            return 3
                        end
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
                    redis.call('del', KEYS[2])
                    redis.call('del', KEYS[3])
                    redis.call('del', KEYS[5])
                    redis.call('set', KEYS[4], ARGV[2], 'PX', ARGV[3])
                    return 2
                    """, Long.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final Duration codeTtl;
    private final Duration sessionTtl;
    private final Duration resetTokenTtl;
    private final Duration resendCooldownTtl;
    private final Duration verifyFailureTtl;
    private final int maxVerifyFailures;

    public PasswordResetRedisRepository(
            RedisTemplate<String, String> redisTemplate,
            @Value("${app.password-reset.code-ttl-minutes:5}") long codeTtlMinutes,
            @Value("${app.password-reset.session-ttl-minutes:10}") long sessionTtlMinutes,
            @Value("${app.password-reset.reset-token-ttl-minutes:10}") long resetTokenTtlMinutes,
            @Value("${app.password-reset.resend-cooldown-seconds:60}") long resendCooldownSeconds,
            @Value("${app.password-reset.verify-failure-ttl-minutes:5}") long verifyFailureTtlMinutes,
            @Value("${app.password-reset.max-verify-failures:5}") int maxVerifyFailures
    ) {
        this.redisTemplate = redisTemplate;
        this.codeTtl = Duration.ofMinutes(codeTtlMinutes);
        this.sessionTtl = Duration.ofMinutes(sessionTtlMinutes);
        this.resetTokenTtl = Duration.ofMinutes(resetTokenTtlMinutes);
        this.resendCooldownTtl = Duration.ofSeconds(resendCooldownSeconds);
        this.verifyFailureTtl = Duration.ofMinutes(verifyFailureTtlMinutes);
        this.maxVerifyFailures = maxVerifyFailures;
    }

    @Override
    public boolean saveCodeIfNotCoolingDown(String email, String code) {
        Long result = redisTemplate.execute(
                SAVE_CODE_IF_NOT_COOLING_DOWN_SCRIPT,
                List.of(
                        codeKey(email),
                        sessionKey(email),
                        resendCooldownKey(email),
                        verifyFailureKey(email)
                ),
                code,
                String.valueOf(codeTtl.toMillis()),
                SESSION_VALUE,
                String.valueOf(sessionTtl.toMillis()),
                COOLDOWN_VALUE,
                String.valueOf(resendCooldownTtl.toMillis())
        );

        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void deleteCodeAndCooldownIfMatches(String email, String code) {
        redisTemplate.execute(
                DELETE_CODE_AND_COOLDOWN_IF_MATCHES_SCRIPT,
                List.of(codeKey(email), sessionKey(email), resendCooldownKey(email)),
                code
        );
    }

    @Override
    public VerifyCodeResult verifyCodeAndSaveResetToken(String email, String code, String resetToken) {
        Long result = redisTemplate.execute(
                VERIFY_CODE_AND_SAVE_RESET_TOKEN_SCRIPT,
                List.of(
                        codeKey(email),
                        sessionKey(email),
                        verifyFailureKey(email),
                        resetTokenKey(resetToken),
                        resendCooldownKey(email)
                ),
                code,
                normalizeEmail(email),
                String.valueOf(resetTokenTtl.toMillis()),
                String.valueOf(maxVerifyFailures),
                String.valueOf(verifyFailureTtl.toMillis())
        );

        return toVerifyCodeResult(result);
    }

    private String codeKey(String email) {
        return CODE_KEY_PREFIX + normalizeEmail(email);
    }

    private String sessionKey(String email) {
        return SESSION_KEY_PREFIX + normalizeEmail(email);
    }

    private String resetTokenKey(String resetToken) {
        return RESET_TOKEN_KEY_PREFIX + resetToken;
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

    private VerifyCodeResult toVerifyCodeResult(Long result) {
        if (Long.valueOf(2L).equals(result)) {
            return VerifyCodeResult.VERIFIED;
        }
        if (Long.valueOf(1L).equals(result)) {
            return VerifyCodeResult.CODE_MISMATCH;
        }
        if (Long.valueOf(3L).equals(result)) {
            return VerifyCodeResult.CODE_EXPIRED;
        }
        if (Long.valueOf(4L).equals(result)) {
            return VerifyCodeResult.VERIFY_ATTEMPT_EXCEEDED;
        }

        return VerifyCodeResult.CODE_NOT_FOUND;
    }
}

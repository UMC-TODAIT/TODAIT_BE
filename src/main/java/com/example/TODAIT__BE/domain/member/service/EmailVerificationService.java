package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationSendRequest;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationVerifyRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationSendResponse;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationVerifyResponse;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import com.example.TODAIT__BE.infra.mail.EmailVerificationAsyncService;
import com.example.TODAIT__BE.infra.redis.EmailVerificationRedisRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class EmailVerificationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final EmailVerificationRedisRepository emailVerificationRedisRepository;
    private final EmailVerificationAsyncService emailVerificationAsyncService;
    private final RandomCodeGenerator randomCodeGenerator;
    private final long codeTtlMinutes;

    public EmailVerificationService(
            EmailVerificationRedisRepository emailVerificationRedisRepository,
            EmailVerificationAsyncService emailVerificationAsyncService,
            RandomCodeGenerator randomCodeGenerator,
            @Value("${app.email-verification.code-ttl-minutes}") long codeTtlMinutes
    ) {
        this.emailVerificationRedisRepository = emailVerificationRedisRepository;
        this.emailVerificationAsyncService = emailVerificationAsyncService;
        this.randomCodeGenerator = randomCodeGenerator;
        this.codeTtlMinutes = codeTtlMinutes;
    }

    public EmailVerificationSendResponse sendVerificationCode(
            EmailVerificationSendRequest request
    ) {
        String email = normalizeAndValidateEmail(request.email());
        if (emailVerificationRedisRepository.isVerified(email)) {
            throw new ProjectException(EmailVerificationErrorCode.ALREADY_COMPLETED);
        }

        String code = randomCodeGenerator.generateNumericCode();
        saveCode(email, code);
        emailVerificationAsyncService.sendVerificationCodeAsync(email, code);

        return new EmailVerificationSendResponse(email, codeTtlMinutes);
    }

    public EmailVerificationVerifyResponse verifyCode(
            EmailVerificationVerifyRequest request
    ) {
        String email = normalizeAndValidateEmail(request.email());
        if (emailVerificationRedisRepository.isVerified(email)) {
            throw new ProjectException(EmailVerificationErrorCode.ALREADY_COMPLETED);
        }

        String savedCode = emailVerificationRedisRepository.findCodeByEmail(email)
                .orElseThrow(() -> new ProjectException(EmailVerificationErrorCode.CODE_NOT_FOUND));

        if (!savedCode.equals(request.code().trim())) {
            throw new ProjectException(EmailVerificationErrorCode.CODE_MISMATCH);
        }

        emailVerificationRedisRepository.deleteCode(email);
        emailVerificationRedisRepository.saveVerified(email);

        return new EmailVerificationVerifyResponse(email, true);
    }

    private void saveCode(String email, String code) {
        try {
            emailVerificationRedisRepository.saveCode(email, code);
        } catch (RuntimeException e) {
            throw new ProjectException(EmailVerificationErrorCode.STORE_FAILED);
        }
    }

    private String normalizeAndValidateEmail(String email) {
        if (email == null) {
            throw new ProjectException(EmailVerificationErrorCode.INVALID_EMAIL_FORMAT);
        }

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new ProjectException(EmailVerificationErrorCode.INVALID_EMAIL_FORMAT);
        }

        return normalizedEmail;
    }
}

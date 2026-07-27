package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationSendRequest;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationVerifyRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationSendResponse;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationVerifyResponse;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import com.example.TODAIT__BE.infra.mail.EmailVerificationAsyncService;
import com.example.TODAIT__BE.infra.redis.EmailVerificationRedisRepository;
import com.example.TODAIT__BE.infra.redis.EmailVerificationRedisRepository.VerifyCodeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {

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

        VerifyCodeResult result = emailVerificationRedisRepository.verifyCodeAndMarkVerified(
                email,
                request.code().trim()
        );
        if (result == VerifyCodeResult.CODE_NOT_FOUND) {
            throw new ProjectException(EmailVerificationErrorCode.CODE_NOT_FOUND);
        }
        if (result == VerifyCodeResult.CODE_MISMATCH) {
            throw new ProjectException(EmailVerificationErrorCode.CODE_MISMATCH);
        }
        if (result == VerifyCodeResult.VERIFY_ATTEMPT_EXCEEDED) {
            throw new ProjectException(EmailVerificationErrorCode.VERIFY_ATTEMPT_EXCEEDED);
        }

        return new EmailVerificationVerifyResponse(email, true);
    }

    private void saveCode(String email, String code) {
        try {
            boolean saved = emailVerificationRedisRepository.saveCodeIfNotCoolingDown(email, code);
            if (!saved) {
                throw new ProjectException(EmailVerificationErrorCode.RESEND_COOLDOWN);
            }
        } catch (RuntimeException e) {
            if (e instanceof ProjectException) {
                throw e;
            }
            throw new ProjectException(EmailVerificationErrorCode.STORE_FAILED);
        }
    }

    private String normalizeAndValidateEmail(String email) {
        if (!MemberInputPolicy.isValidEmail(email)) {
            throw new ProjectException(EmailVerificationErrorCode.INVALID_EMAIL_FORMAT);
        }

        return MemberInputPolicy.normalizeEmail(email);
    }
}

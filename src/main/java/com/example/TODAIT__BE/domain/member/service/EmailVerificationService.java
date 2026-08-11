package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationResponse;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.service.port.EmailVerificationSender;
import com.example.TODAIT__BE.domain.member.service.port.EmailVerificationStore;
import com.example.TODAIT__BE.domain.member.service.port.EmailVerificationStore.VerifyCodeResult;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {

    private final EmailVerificationStore emailVerificationStore;
    private final EmailVerificationSender emailVerificationSender;
    private final RandomCodeGenerator randomCodeGenerator;
    private final MemberRepository memberRepository;
    private final long codeTtlMinutes;

    public EmailVerificationService(
            EmailVerificationStore emailVerificationStore,
            EmailVerificationSender emailVerificationSender,
            RandomCodeGenerator randomCodeGenerator,
            MemberRepository memberRepository,
            @Value("${app.email-verification.code-ttl-minutes}") long codeTtlMinutes
    ) {
        this.emailVerificationStore = emailVerificationStore;
        this.emailVerificationSender = emailVerificationSender;
        this.randomCodeGenerator = randomCodeGenerator;
        this.memberRepository = memberRepository;
        this.codeTtlMinutes = codeTtlMinutes;
    }

    public EmailVerificationResponse.Send sendVerificationCode(
            EmailVerificationRequest.Send request
    ) {
        String email = normalizeAndValidateEmail(request.email());
        if (memberRepository.existsByEmail(email)) {
            return new EmailVerificationResponse.Send(email, codeTtlMinutes);
        }

        String code = randomCodeGenerator.generateNumericCode();
        saveCode(email, code);
        emailVerificationSender.sendVerificationCode(email, code);

        return new EmailVerificationResponse.Send(email, codeTtlMinutes);
    }

    public EmailVerificationResponse.Verify verifyCode(
            EmailVerificationRequest.Verify request
    ) {
        String email = normalizeAndValidateEmail(request.email());
        if (emailVerificationStore.isVerified(email)) {
            throw new MemberException(EmailVerificationErrorCode.ALREADY_COMPLETED);
        }

        VerifyCodeResult result = emailVerificationStore.verifyCodeAndMarkVerified(
                email,
                request.code().trim()
        );
        if (result == VerifyCodeResult.CODE_NOT_FOUND) {
            throw new MemberException(EmailVerificationErrorCode.CODE_NOT_FOUND);
        }
        if (result == VerifyCodeResult.CODE_MISMATCH) {
            throw new MemberException(EmailVerificationErrorCode.CODE_MISMATCH);
        }
        if (result == VerifyCodeResult.VERIFY_ATTEMPT_EXCEEDED) {
            throw new MemberException(EmailVerificationErrorCode.VERIFY_ATTEMPT_EXCEEDED);
        }

        return new EmailVerificationResponse.Verify(email, true);
    }

    private void saveCode(String email, String code) {
        try {
            boolean saved = emailVerificationStore.saveCodeIfNotCoolingDown(email, code);
            if (!saved) {
                throw new MemberException(EmailVerificationErrorCode.RESEND_COOLDOWN);
            }
        } catch (RuntimeException e) {
            if (e instanceof ProjectException) {
                throw e;
            }
            throw new MemberException(EmailVerificationErrorCode.STORE_FAILED, e);
        }
    }

    private String normalizeAndValidateEmail(String email) {
        if (!MemberInputPolicy.isValidEmail(email)) {
            throw new MemberException(EmailVerificationErrorCode.INVALID_EMAIL_FORMAT);
        }

        return MemberInputPolicy.normalizeEmail(email);
    }
}

package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.PasswordResetErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.PasswordResetRequest;
import com.example.TODAIT__BE.domain.member.dto.response.PasswordResetResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetSender;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetStore;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {

    private final MemberRepository memberRepository;
    private final PasswordResetStore passwordResetStore;
    private final PasswordResetSender passwordResetSender;
    private final RandomCodeGenerator randomCodeGenerator;

    public PasswordResetService(
            MemberRepository memberRepository,
            PasswordResetStore passwordResetStore,
            PasswordResetSender passwordResetSender,
            RandomCodeGenerator randomCodeGenerator
    ) {
        this.memberRepository = memberRepository;
        this.passwordResetStore = passwordResetStore;
        this.passwordResetSender = passwordResetSender;
        this.randomCodeGenerator = randomCodeGenerator;
    }

    public PasswordResetResponse.Send sendPasswordResetCode(
            PasswordResetRequest.Send request
    ) {
        String email = normalizeAndValidateEmail(request.email());
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(PasswordResetErrorCode.EMAIL_NOT_FOUND));
        validateEmailMember(member);

        String code = randomCodeGenerator.generateNumericCode();
        saveCode(email, code);
        passwordResetSender.sendPasswordResetCode(email, code);

        return new PasswordResetResponse.Send();
    }

    private void validateEmailMember(Member member) {
        String passwordHash = member.getPasswordHash();
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new MemberException(PasswordResetErrorCode.EMAIL_MEMBER_ONLY);
        }
    }

    private void saveCode(String email, String code) {
        try {
            boolean saved = passwordResetStore.saveCodeIfNotCoolingDown(email, code);
            if (!saved) {
                throw new MemberException(PasswordResetErrorCode.RESEND_COOLDOWN);
            }
        } catch (RuntimeException e) {
            if (e instanceof ProjectException) {
                throw e;
            }
            throw new MemberException(PasswordResetErrorCode.STORE_FAILED, e);
        }
    }

    private String normalizeAndValidateEmail(String email) {
        if (!MemberInputPolicy.isValidEmail(email)) {
            throw new MemberException(PasswordResetErrorCode.INVALID_EMAIL_FORMAT);
        }

        return MemberInputPolicy.normalizeEmail(email);
    }
}

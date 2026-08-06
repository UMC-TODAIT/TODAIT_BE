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
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

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
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (!canSendPasswordResetCode(member)) {
            return new PasswordResetResponse.Send();
        }

        String code = randomCodeGenerator.generateNumericCode();
        if (saveCode(email, code)) {
            sendCode(email, code);
        }

        return new PasswordResetResponse.Send();
    }

    private boolean canSendPasswordResetCode(Member member) {
        if (member == null) {
            return false;
        }

        String passwordHash = member.getPasswordHash();
        return passwordHash != null && !passwordHash.isBlank();
    }

    private boolean saveCode(String email, String code) {
        try {
            return passwordResetStore.saveCodeIfNotCoolingDown(email, code);
        } catch (RuntimeException e) {
            log.warn("비밀번호 재설정 인증번호 저장에 실패했습니다. email={}", email, e);
            return false;
        }
    }

    private void sendCode(String email, String code) {
        try {
            passwordResetSender.sendPasswordResetCode(email, code);
        } catch (RuntimeException e) {
            log.warn("비밀번호 재설정 인증번호 발송에 실패했습니다. email={}", email, e);
        }
    }

    private String normalizeAndValidateEmail(String email) {
        if (!MemberInputPolicy.isValidEmail(email)) {
            throw new MemberException(PasswordResetErrorCode.INVALID_EMAIL_FORMAT);
        }

        return MemberInputPolicy.normalizeEmail(email);
    }
}

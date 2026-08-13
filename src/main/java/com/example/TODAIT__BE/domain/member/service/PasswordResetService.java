package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.PasswordResetErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.PasswordResetRequest;
import com.example.TODAIT__BE.domain.member.dto.response.PasswordResetResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.repository.RefreshTokenRepository;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetSender;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetStore;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetStore.ConsumeResetTokenResult;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetStore.ConsumeResetTokenStatus;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetStore.VerifyCodeResult;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetStore passwordResetStore;
    private final PasswordResetSender passwordResetSender;
    private final RandomCodeGenerator randomCodeGenerator;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            MemberRepository memberRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetStore passwordResetStore,
            PasswordResetSender passwordResetSender,
            RandomCodeGenerator randomCodeGenerator,
            PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetStore = passwordResetStore;
        this.passwordResetSender = passwordResetSender;
        this.randomCodeGenerator = randomCodeGenerator;
        this.passwordEncoder = passwordEncoder;
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

    public PasswordResetResponse.Verify verifyPasswordResetCode(
            PasswordResetRequest.Verify request
    ) {
        String email = normalizeAndValidateEmail(request.email());
        String resetToken = randomCodeGenerator.generateUrlSafeToken();
        VerifyCodeResult result = verifyCode(email, request.code(), resetToken);

        if (result == VerifyCodeResult.CODE_NOT_FOUND
                || result == VerifyCodeResult.CODE_EXPIRED
                || result == VerifyCodeResult.CODE_MISMATCH) {
            throw new MemberException(PasswordResetErrorCode.CODE_MISMATCH);
        }
        if (result == VerifyCodeResult.VERIFY_ATTEMPT_EXCEEDED) {
            throw new MemberException(PasswordResetErrorCode.VERIFY_ATTEMPT_EXCEEDED);
        }

        return new PasswordResetResponse.Verify(resetToken);
    }

    @Transactional
    public PasswordResetResponse.SetNewPassword setNewPassword(
            PasswordResetRequest.SetNewPassword request
    ) {
        if (!request.newPassword().equals(request.newPasswordCheck())) {
            throw new MemberException(PasswordResetErrorCode.NEW_PASSWORD_MISMATCH);
        }

        ConsumeResetTokenResult result = claimResetToken(request.resetToken());
        if (result.status() == ConsumeResetTokenStatus.INVALID) {
            throw new MemberException(PasswordResetErrorCode.INVALID_RESET_TOKEN);
        }
        if (result.status() == ConsumeResetTokenStatus.EXPIRED) {
            throw new MemberException(PasswordResetErrorCode.RESET_TOKEN_EXPIRED);
        }

        Member member = memberRepository.findByEmail(result.email())
                .orElseThrow(() -> new MemberException(PasswordResetErrorCode.INVALID_RESET_TOKEN));
        if (!canSendPasswordResetCode(member)) {
            throw new MemberException(PasswordResetErrorCode.EMAIL_MEMBER_ONLY);
        }

        member.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.findAllByMemberAndRevokedAtIsNull(member)
                .forEach(RefreshToken::revoke);
        consumeResetTokenAfterCommit(request.resetToken());

        return new PasswordResetResponse.SetNewPassword();
    }

    private ConsumeResetTokenResult claimResetToken(String resetToken) {
        try {
            return passwordResetStore.claimResetToken(resetToken);
        } catch (RuntimeException e) {
            if (e instanceof ProjectException) {
                throw e;
            }
            throw new MemberException(PasswordResetErrorCode.STORE_FAILED, e);
        }
    }

    private void consumeResetTokenAfterCommit(String resetToken) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            consumeResetToken(resetToken);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                consumeResetToken(resetToken);
            }
        });
    }

    private void consumeResetToken(String resetToken) {
        try {
            passwordResetStore.consumeResetToken(resetToken);
        } catch (RuntimeException e) {
            log.warn("resetToken 소비에 실패했습니다.", e);
        }
    }

    private VerifyCodeResult verifyCode(String email, String code, String resetToken) {
        if (code == null) {
            return VerifyCodeResult.CODE_MISMATCH;
        }

        try {
            return passwordResetStore.verifyCodeAndSaveResetToken(email, code, resetToken);
        } catch (RuntimeException e) {
            if (e instanceof ProjectException) {
                throw e;
            }
            throw new MemberException(PasswordResetErrorCode.STORE_FAILED, e);
        }
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
            log.warn("비밀번호 재설정 인증번호 저장에 실패했습니다. email={}", maskEmail(email), e);
            return false;
        }
    }

    private void sendCode(String email, String code) {
        try {
            passwordResetSender.sendPasswordResetCode(email, code);
        } catch (RuntimeException e) {
            log.warn("비밀번호 재설정 인증번호 발송에 실패했습니다. email={}", maskEmail(email), e);
        }
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }

        return email.charAt(0) + "***" + email.substring(atIndex);
    }

    private String normalizeAndValidateEmail(String email) {
        if (!MemberInputPolicy.isValidEmail(email)) {
            throw new MemberException(PasswordResetErrorCode.INVALID_EMAIL_FORMAT);
        }

        return MemberInputPolicy.normalizeEmail(email);
    }
}

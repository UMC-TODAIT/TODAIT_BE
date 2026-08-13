package com.example.TODAIT__BE.infra.mail;

import com.example.TODAIT__BE.domain.member.service.port.EmailVerificationSender;
import com.example.TODAIT__BE.domain.member.service.port.EmailVerificationStore;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationAsyncService implements EmailVerificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationAsyncService.class);
    private static final String SUBJECT = "[TODAIT] 이메일 인증번호 안내";

    private final MailSender mailSender;
    private final EmailVerificationStore emailVerificationStore;

    public EmailVerificationAsyncService(
            MailSender mailSender,
            EmailVerificationStore emailVerificationStore
    ) {
        this.mailSender = mailSender;
        this.emailVerificationStore = emailVerificationStore;
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendVerificationCode(String email, String code) {
        try {
            mailSender.send(email, SUBJECT, createVerificationText(code));
        } catch (ProjectException e) {
            emailVerificationStore.deleteCodeAndCooldownIfMatches(email, code);
            log.warn("Failed to send email verification code. email={}", maskEmail(email), e);
        }
    }

    private String createVerificationText(String code) {
        return """
                안녕하세요. TODAIT입니다.

                이메일 인증번호는 아래와 같습니다.

                인증번호: %s

                인증번호는 제한 시간 내에 입력해주세요.
                본인이 요청하지 않은 메일이라면 이 메일을 무시해주세요.
                """.formatted(code);
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }

        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}

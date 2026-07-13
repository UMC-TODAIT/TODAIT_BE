package com.example.TODAIT__BE.infra.mail;

import org.springframework.stereotype.Service;

@Service
public class EmailVerificationMailService {

    private static final String SUBJECT = "[TODAIT] 이메일 인증번호 안내";

    private final MailSender mailSender;

    public EmailVerificationMailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationCode(String email, String code) {
        mailSender.send(email, SUBJECT, createVerificationText(code));
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
}

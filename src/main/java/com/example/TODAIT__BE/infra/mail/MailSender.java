package com.example.TODAIT__BE.infra.mail;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("todaitMailSender")
public class MailSender {

    private final JavaMailSender javaMailSender;
    private final String from;

    public MailSender(
            JavaMailSender javaMailSender,
            @Value("${app.mail.from:}") String from
    ) {
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    public void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (!from.isBlank()) {
                message.setFrom(from);
            }
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            javaMailSender.send(message);
        } catch (MailException e) {
            throw new ProjectException(EmailVerificationErrorCode.SEND_FAILED);
        }
    }
}

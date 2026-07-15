package com.example.TODAIT__BE.infra.mail;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationAsyncServiceTest {

    @Mock
    private MailSender mailSender;

    @Test
    void sendVerificationCodeAsyncDelegatesToMailSender() {
        EmailVerificationAsyncService asyncService = new EmailVerificationAsyncService(mailSender);

        asyncService.sendVerificationCodeAsync("test@example.com", "123456");

        verify(mailSender).send(
                eq("test@example.com"),
                eq("[TODAIT] 이메일 인증번호 안내"),
                contains("123456")
        );
    }

    @Test
    void sendVerificationCodeAsyncDoesNotPropagateMailFailure() {
        EmailVerificationAsyncService asyncService = new EmailVerificationAsyncService(mailSender);
        willThrow(new ProjectException(EmailVerificationErrorCode.SEND_FAILED))
                .given(mailSender)
                .send(eq("test@example.com"), eq("[TODAIT] 이메일 인증번호 안내"), anyString());

        assertThatCode(() -> asyncService.sendVerificationCodeAsync("test@example.com", "123456"))
                .doesNotThrowAnyException();
    }
}

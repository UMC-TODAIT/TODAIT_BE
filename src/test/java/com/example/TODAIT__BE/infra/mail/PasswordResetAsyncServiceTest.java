package com.example.TODAIT__BE.infra.mail;

import com.example.TODAIT__BE.domain.member.code.PasswordResetErrorCode;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetStore;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetAsyncServiceTest {

    @Mock
    private MailSender mailSender;

    @Mock
    private PasswordResetStore passwordResetStore;

    @Test
    void sendPasswordResetCodeAsyncDelegatesToMailSender() {
        PasswordResetAsyncService asyncService = new PasswordResetAsyncService(
                mailSender,
                passwordResetStore
        );

        asyncService.sendPasswordResetCode("test@example.com", "123456");

        verify(mailSender).send(
                eq("test@example.com"),
                eq("[TODAIT] 비밀번호 재설정 인증번호 안내"),
                contains("123456")
        );
        verify(passwordResetStore, never()).deleteCodeAndCooldownIfMatches(anyString(), anyString());
    }

    @Test
    void sendPasswordResetCodeAsyncDoesNotPropagateMailFailure() {
        PasswordResetAsyncService asyncService = new PasswordResetAsyncService(
                mailSender,
                passwordResetStore
        );
        willThrow(new ProjectException(PasswordResetErrorCode.SEND_FAILED))
                .given(mailSender)
                .send(eq("test@example.com"), eq("[TODAIT] 비밀번호 재설정 인증번호 안내"), anyString());

        assertThatCode(() -> asyncService.sendPasswordResetCode("test@example.com", "123456"))
                .doesNotThrowAnyException();
        verify(passwordResetStore).deleteCodeAndCooldownIfMatches("test@example.com", "123456");
    }
}

package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationResponse;
import com.example.TODAIT__BE.domain.member.exception.EmailVerificationException;
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import com.example.TODAIT__BE.infra.mail.EmailVerificationAsyncService;
import com.example.TODAIT__BE.infra.redis.EmailVerificationRedisRepository;
import com.example.TODAIT__BE.infra.redis.EmailVerificationRedisRepository.VerifyCodeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final long CODE_TTL_MINUTES = 5L;

    @Mock
    private EmailVerificationRedisRepository emailVerificationRedisRepository;

    @Mock
    private EmailVerificationAsyncService emailVerificationAsyncService;

    @Mock
    private RandomCodeGenerator randomCodeGenerator;

    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationService(
                emailVerificationRedisRepository,
                emailVerificationAsyncService,
                randomCodeGenerator,
                CODE_TTL_MINUTES
        );
    }

    @Test
    void sendVerificationCodeSavesCodeAndSendsMail() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(randomCodeGenerator.generateNumericCode())
                .willReturn("123456");
        given(emailVerificationRedisRepository.saveCodeIfNotCoolingDown("test@example.com", "123456"))
                .willReturn(true);

        EmailVerificationResponse.Send response = emailVerificationService.sendVerificationCode(
                new EmailVerificationRequest.Send(" Test@Example.com ")
        );

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.expiresInMinutes()).isEqualTo(CODE_TTL_MINUTES);
        verify(emailVerificationRedisRepository).saveCodeIfNotCoolingDown("test@example.com", "123456");
        verify(emailVerificationAsyncService).sendVerificationCodeAsync("test@example.com", "123456");
    }

    @Test
    void sendVerificationCodeRejectsInvalidEmail() {
        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationRequest.Send("invalid-email")
        ))
                .isInstanceOf(EmailVerificationException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.INVALID_EMAIL_FORMAT);

        verify(emailVerificationRedisRepository, never()).saveCodeIfNotCoolingDown(anyString(), anyString());
        verify(emailVerificationAsyncService, never()).sendVerificationCodeAsync(anyString(), anyString());
    }

    @Test
    void sendVerificationCodeFailsWhenResendCooldownIsActive() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(randomCodeGenerator.generateNumericCode())
                .willReturn("123456");
        given(emailVerificationRedisRepository.saveCodeIfNotCoolingDown("test@example.com", "123456"))
                .willReturn(false);

        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationRequest.Send("test@example.com")
        ))
                .isInstanceOf(EmailVerificationException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.RESEND_COOLDOWN);

        verify(emailVerificationAsyncService, never()).sendVerificationCodeAsync(anyString(), anyString());
    }

    @Test
    void verifyCodeStoresVerifiedStateAndDeletesCode() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationRedisRepository.verifyCodeAndMarkVerified("test@example.com", "123456"))
                .willReturn(VerifyCodeResult.VERIFIED);

        EmailVerificationResponse.Verify response = emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify(" Test@Example.com ", " 123456 ")
        );

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.verified()).isTrue();
        verify(emailVerificationRedisRepository).verifyCodeAndMarkVerified("test@example.com", "123456");
    }

    @Test
    void verifyCodeFailsWhenCodeMismatches() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationRedisRepository.verifyCodeAndMarkVerified("test@example.com", "000000"))
                .willReturn(VerifyCodeResult.CODE_MISMATCH);

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify("test@example.com", "000000")
        ))
                .isInstanceOf(EmailVerificationException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.CODE_MISMATCH);

        verify(emailVerificationRedisRepository).verifyCodeAndMarkVerified("test@example.com", "000000");
    }

    @Test
    void verifyCodeFailsWhenCodeDoesNotExist() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationRedisRepository.verifyCodeAndMarkVerified("test@example.com", "123456"))
                .willReturn(VerifyCodeResult.CODE_NOT_FOUND);

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(EmailVerificationException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.CODE_NOT_FOUND);
    }

    @Test
    void verifyCodeFailsWhenVerifyAttemptExceeded() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationRedisRepository.verifyCodeAndMarkVerified("test@example.com", "123456"))
                .willReturn(VerifyCodeResult.VERIFY_ATTEMPT_EXCEEDED);

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(EmailVerificationException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.VERIFY_ATTEMPT_EXCEEDED);
    }

    @Test
    void sendVerificationCodeFailsWhenEmailAlreadyVerified() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(true);

        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationRequest.Send("test@example.com")
        ))
                .isInstanceOf(EmailVerificationException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.ALREADY_COMPLETED);

        verify(emailVerificationRedisRepository, never()).saveCodeIfNotCoolingDown(anyString(), anyString());
        verify(emailVerificationAsyncService, never()).sendVerificationCodeAsync(anyString(), anyString());
    }

    @Test
    void verifyCodeFailsWhenEmailAlreadyVerified() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(true);

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(EmailVerificationException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.ALREADY_COMPLETED);

        verify(emailVerificationRedisRepository, never()).verifyCodeAndMarkVerified(anyString(), anyString());
    }

}

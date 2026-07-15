package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationSendRequest;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationVerifyRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationSendResponse;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationVerifyResponse;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import com.example.TODAIT__BE.infra.mail.EmailVerificationAsyncService;
import com.example.TODAIT__BE.infra.redis.EmailVerificationRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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

        EmailVerificationSendResponse response = emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest(" Test@Example.com ")
        );

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.expiresInMinutes()).isEqualTo(CODE_TTL_MINUTES);
        verify(emailVerificationRedisRepository).saveCode("test@example.com", "123456");
        verify(emailVerificationAsyncService).sendVerificationCodeAsync("test@example.com", "123456");
    }

    @Test
    void sendVerificationCodeRejectsInvalidEmail() {
        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest("invalid-email")
        ))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.INVALID_EMAIL_FORMAT);

        verify(emailVerificationRedisRepository, never()).saveCode(anyString(), anyString());
        verify(emailVerificationAsyncService, never()).sendVerificationCodeAsync(anyString(), anyString());
    }

    @Test
    void sendVerificationCodeUpdatesCodeWhenSameEmailRequestsAgain() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(randomCodeGenerator.generateNumericCode())
                .willReturn("111111", "222222");

        emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest("test@example.com")
        );
        emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest("TEST@example.com")
        );

        verify(emailVerificationRedisRepository).saveCode("test@example.com", "111111");
        verify(emailVerificationRedisRepository).saveCode("test@example.com", "222222");
        verify(emailVerificationAsyncService).sendVerificationCodeAsync("test@example.com", "111111");
        verify(emailVerificationAsyncService).sendVerificationCodeAsync("test@example.com", "222222");
    }

    @Test
    void verifyCodeStoresVerifiedStateAndDeletesCode() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationRedisRepository.findCodeByEmail("test@example.com"))
                .willReturn(Optional.of("123456"));

        EmailVerificationVerifyResponse response = emailVerificationService.verifyCode(
                new EmailVerificationVerifyRequest(" Test@Example.com ", " 123456 ")
        );

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.verified()).isTrue();
        verify(emailVerificationRedisRepository).deleteCode("test@example.com");
        verify(emailVerificationRedisRepository).saveVerified("test@example.com");
    }

    @Test
    void verifyCodeFailsWhenCodeMismatches() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationRedisRepository.findCodeByEmail("test@example.com"))
                .willReturn(Optional.of("123456"));

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationVerifyRequest("test@example.com", "000000")
        ))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.CODE_MISMATCH);

        verify(emailVerificationRedisRepository, never()).deleteCode("test@example.com");
        verify(emailVerificationRedisRepository, never()).saveVerified("test@example.com");
    }

    @Test
    void verifyCodeFailsWhenCodeDoesNotExist() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationRedisRepository.findCodeByEmail("test@example.com"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationVerifyRequest("test@example.com", "123456")
        ))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.CODE_NOT_FOUND);
    }

    @Test
    void sendVerificationCodeFailsWhenEmailAlreadyVerified() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(true);

        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationSendRequest("test@example.com")
        ))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.ALREADY_COMPLETED);

        verify(emailVerificationRedisRepository, never()).saveCode(anyString(), anyString());
        verify(emailVerificationAsyncService, never()).sendVerificationCodeAsync(anyString(), anyString());
    }

    @Test
    void verifyCodeFailsWhenEmailAlreadyVerified() {
        given(emailVerificationRedisRepository.isVerified("test@example.com"))
                .willReturn(true);

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationVerifyRequest("test@example.com", "123456")
        ))
                .isInstanceOf(ProjectException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.ALREADY_COMPLETED);

        verify(emailVerificationRedisRepository, never()).findCodeByEmail("test@example.com");
    }

}

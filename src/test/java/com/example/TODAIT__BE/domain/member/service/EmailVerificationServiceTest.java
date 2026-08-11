package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.EmailVerificationRequest;
import com.example.TODAIT__BE.domain.member.dto.response.EmailVerificationResponse;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.service.port.EmailVerificationSender;
import com.example.TODAIT__BE.domain.member.service.port.EmailVerificationStore;
import com.example.TODAIT__BE.domain.member.service.port.EmailVerificationStore.VerifyCodeResult;
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
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
    private EmailVerificationStore emailVerificationStore;

    @Mock
    private EmailVerificationSender emailVerificationSender;

    @Mock
    private RandomCodeGenerator randomCodeGenerator;

    @Mock
    private MemberRepository memberRepository;

    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        emailVerificationService = new EmailVerificationService(
                emailVerificationStore,
                emailVerificationSender,
                randomCodeGenerator,
                memberRepository,
                CODE_TTL_MINUTES
        );
    }

    @Test
    void sendVerificationCodeSavesCodeAndSendsMail() {
        given(memberRepository.existsByEmail("test@example.com"))
                .willReturn(false);
        given(randomCodeGenerator.generateNumericCode())
                .willReturn("123456");
        given(emailVerificationStore.saveCodeIfNotCoolingDown("test@example.com", "123456"))
                .willReturn(true);

        EmailVerificationResponse.Send response = emailVerificationService.sendVerificationCode(
                new EmailVerificationRequest.Send(" Test@Example.com ")
        );

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.expiresInMinutes()).isEqualTo(CODE_TTL_MINUTES);
        verify(emailVerificationStore).saveCodeIfNotCoolingDown("test@example.com", "123456");
        verify(emailVerificationSender).sendVerificationCode("test@example.com", "123456");
    }

    @Test
    void sendVerificationCodeRejectsInvalidEmail() {
        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationRequest.Send("invalid-email")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.INVALID_EMAIL_FORMAT);

        verify(emailVerificationStore, never()).saveCodeIfNotCoolingDown(anyString(), anyString());
        verify(emailVerificationSender, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void sendVerificationCodeFailsWhenResendCooldownIsActive() {
        given(memberRepository.existsByEmail("test@example.com"))
                .willReturn(false);
        given(randomCodeGenerator.generateNumericCode())
                .willReturn("123456");
        given(emailVerificationStore.saveCodeIfNotCoolingDown("test@example.com", "123456"))
                .willReturn(false);

        assertThatThrownBy(() -> emailVerificationService.sendVerificationCode(
                new EmailVerificationRequest.Send("test@example.com")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.RESEND_COOLDOWN);

        verify(emailVerificationSender, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void verifyCodeStoresVerifiedStateAndDeletesCode() {
        given(emailVerificationStore.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationStore.verifyCodeAndMarkVerified("test@example.com", "123456"))
                .willReturn(VerifyCodeResult.VERIFIED);

        EmailVerificationResponse.Verify response = emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify(" Test@Example.com ", " 123456 ")
        );

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.verified()).isTrue();
        verify(emailVerificationStore).verifyCodeAndMarkVerified("test@example.com", "123456");
    }

    @Test
    void verifyCodeFailsWhenCodeMismatches() {
        given(emailVerificationStore.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationStore.verifyCodeAndMarkVerified("test@example.com", "000000"))
                .willReturn(VerifyCodeResult.CODE_MISMATCH);

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify("test@example.com", "000000")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.CODE_MISMATCH);

        verify(emailVerificationStore).verifyCodeAndMarkVerified("test@example.com", "000000");
    }

    @Test
    void verifyCodeFailsWithMismatchWhenCodeDoesNotExist() {
        given(emailVerificationStore.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationStore.verifyCodeAndMarkVerified("test@example.com", "123456"))
                .willReturn(VerifyCodeResult.CODE_NOT_FOUND);

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.CODE_MISMATCH);
    }

    @Test
    void verifyCodeFailsWithMismatchWhenVerifyAttemptExceeded() {
        given(emailVerificationStore.isVerified("test@example.com"))
                .willReturn(false);
        given(emailVerificationStore.verifyCodeAndMarkVerified("test@example.com", "123456"))
                .willReturn(VerifyCodeResult.VERIFY_ATTEMPT_EXCEEDED);

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.CODE_MISMATCH);
    }

    @Test
    void sendVerificationCodeReturnsSuccessWithoutSendingWhenEmailAlreadyRegistered() {
        given(memberRepository.existsByEmail("test@example.com"))
                .willReturn(true);

        EmailVerificationResponse.Send response = emailVerificationService.sendVerificationCode(
                new EmailVerificationRequest.Send("test@example.com")
        );

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.expiresInMinutes()).isEqualTo(CODE_TTL_MINUTES);
        verify(randomCodeGenerator, never()).generateNumericCode();
        verify(emailVerificationStore, never()).saveCodeIfNotCoolingDown(anyString(), anyString());
        verify(emailVerificationSender, never()).sendVerificationCode(anyString(), anyString());
    }

    @Test
    void sendVerificationCodeAllowsVerifiedEmailBeforeSignup() {
        given(memberRepository.existsByEmail("test@example.com"))
                .willReturn(false);
        given(randomCodeGenerator.generateNumericCode())
                .willReturn("123456");
        given(emailVerificationStore.saveCodeIfNotCoolingDown("test@example.com", "123456"))
                .willReturn(true);

        emailVerificationService.sendVerificationCode(
                new EmailVerificationRequest.Send("test@example.com")
        );

        verify(emailVerificationStore, never()).isVerified("test@example.com");
        verify(emailVerificationSender).sendVerificationCode("test@example.com", "123456");
    }

    @Test
    void verifyCodeFailsWhenEmailAlreadyVerified() {
        given(emailVerificationStore.isVerified("test@example.com"))
                .willReturn(true);

        assertThatThrownBy(() -> emailVerificationService.verifyCode(
                new EmailVerificationRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(EmailVerificationErrorCode.ALREADY_COMPLETED);

        verify(emailVerificationStore, never()).verifyCodeAndMarkVerified(anyString(), anyString());
    }

}

package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.PasswordResetErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.PasswordResetRequest;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetSender;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetStore;
import com.example.TODAIT__BE.domain.member.service.port.PasswordResetStore.VerifyCodeResult;
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordResetStore passwordResetStore;
    @Mock
    private PasswordResetSender passwordResetSender;
    @Mock
    private RandomCodeGenerator randomCodeGenerator;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                memberRepository,
                passwordResetStore,
                passwordResetSender,
                randomCodeGenerator
        );
    }

    @Test
    void sendPasswordResetCodeSavesCodeAndSendsMail() {
        given(memberRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(emailMember()));
        given(randomCodeGenerator.generateNumericCode()).willReturn("123456");
        given(passwordResetStore.saveCodeIfNotCoolingDown("test@example.com", "123456"))
                .willReturn(true);

        passwordResetService.sendPasswordResetCode(
                new PasswordResetRequest.Send(" Test@Example.com ")
        );

        verify(passwordResetStore).saveCodeIfNotCoolingDown("test@example.com", "123456");
        verify(passwordResetSender).sendPasswordResetCode("test@example.com", "123456");
    }

    @Test
    void sendPasswordResetCodeRejectsInvalidEmail() {
        assertThatThrownBy(() -> passwordResetService.sendPasswordResetCode(
                new PasswordResetRequest.Send("invalid-email")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.INVALID_EMAIL_FORMAT);

        verify(memberRepository, never()).findByEmail(anyString());
        verify(passwordResetStore, never()).saveCodeIfNotCoolingDown(anyString(), anyString());
        verify(passwordResetSender, never()).sendPasswordResetCode(anyString(), anyString());
    }

    @Test
    void sendPasswordResetCodeReturnsSuccessWhenMemberDoesNotExist() {
        given(memberRepository.findByEmail("test@example.com"))
                .willReturn(Optional.empty());

        passwordResetService.sendPasswordResetCode(
                new PasswordResetRequest.Send("test@example.com")
        );

        verify(passwordResetStore, never()).saveCodeIfNotCoolingDown(anyString(), anyString());
        verify(passwordResetSender, never()).sendPasswordResetCode(anyString(), anyString());
    }

    @Test
    void sendPasswordResetCodeReturnsSuccessForSocialOnlyMember() {
        given(memberRepository.findByEmail("social@example.com"))
                .willReturn(Optional.of(socialOnlyMember()));

        passwordResetService.sendPasswordResetCode(
                new PasswordResetRequest.Send("social@example.com")
        );

        verify(passwordResetStore, never()).saveCodeIfNotCoolingDown(anyString(), anyString());
        verify(passwordResetSender, never()).sendPasswordResetCode(anyString(), anyString());
    }

    @Test
    void sendPasswordResetCodeReturnsSuccessWhenResendCooldownIsActive() {
        given(memberRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(emailMember()));
        given(randomCodeGenerator.generateNumericCode()).willReturn("123456");
        given(passwordResetStore.saveCodeIfNotCoolingDown("test@example.com", "123456"))
                .willReturn(false);

        passwordResetService.sendPasswordResetCode(
                new PasswordResetRequest.Send("test@example.com")
        );

        verify(passwordResetSender, never()).sendPasswordResetCode(anyString(), anyString());
    }

    @Test
    void sendPasswordResetCodeReturnsSuccessWhenStoreThrowsUnexpectedException() {
        given(memberRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(emailMember()));
        given(randomCodeGenerator.generateNumericCode()).willReturn("123456");
        willThrow(new IllegalStateException("redis down"))
                .given(passwordResetStore)
                .saveCodeIfNotCoolingDown("test@example.com", "123456");

        passwordResetService.sendPasswordResetCode(
                new PasswordResetRequest.Send("test@example.com")
        );

        verify(passwordResetSender, never()).sendPasswordResetCode(anyString(), anyString());
    }

    @Test
    void verifyPasswordResetCodeSavesResetToken() {
        given(randomCodeGenerator.generateUrlSafeToken()).willReturn("reset-token");
        given(passwordResetStore.verifyCodeAndSaveResetToken("test@example.com", "123456", "reset-token"))
                .willReturn(VerifyCodeResult.VERIFIED);

        var response = passwordResetService.verifyPasswordResetCode(
                new PasswordResetRequest.Verify(" Test@Example.com ", " 123456 ")
        );

        assertThat(response.resetToken()).isEqualTo("reset-token");
        verify(passwordResetStore).verifyCodeAndSaveResetToken("test@example.com", "123456", "reset-token");
    }

    @Test
    void verifyPasswordResetCodeFailsWhenCodeDoesNotExist() {
        given(randomCodeGenerator.generateUrlSafeToken()).willReturn("reset-token");
        given(passwordResetStore.verifyCodeAndSaveResetToken("test@example.com", "123456", "reset-token"))
                .willReturn(VerifyCodeResult.CODE_NOT_FOUND);

        assertThatThrownBy(() -> passwordResetService.verifyPasswordResetCode(
                new PasswordResetRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.CODE_MISMATCH);
    }

    @Test
    void verifyPasswordResetCodeFailsWhenCodeExpired() {
        given(randomCodeGenerator.generateUrlSafeToken()).willReturn("reset-token");
        given(passwordResetStore.verifyCodeAndSaveResetToken("test@example.com", "123456", "reset-token"))
                .willReturn(VerifyCodeResult.CODE_EXPIRED);

        assertThatThrownBy(() -> passwordResetService.verifyPasswordResetCode(
                new PasswordResetRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.CODE_MISMATCH);
    }

    @Test
    void verifyPasswordResetCodeFailsWhenCodeIsNull() {
        assertThatThrownBy(() -> passwordResetService.verifyPasswordResetCode(
                new PasswordResetRequest.Verify("test@example.com", null)
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.CODE_MISMATCH);

        verify(passwordResetStore, never()).verifyCodeAndSaveResetToken(anyString(), anyString(), anyString());
    }

    @Test
    void verifyPasswordResetCodeFailsWhenCodeMismatches() {
        given(randomCodeGenerator.generateUrlSafeToken()).willReturn("reset-token");
        given(passwordResetStore.verifyCodeAndSaveResetToken("test@example.com", "000000", "reset-token"))
                .willReturn(VerifyCodeResult.CODE_MISMATCH);

        assertThatThrownBy(() -> passwordResetService.verifyPasswordResetCode(
                new PasswordResetRequest.Verify("test@example.com", "000000")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.CODE_MISMATCH);
    }

    @Test
    void verifyPasswordResetCodeFailsWhenVerifyAttemptExceeded() {
        given(randomCodeGenerator.generateUrlSafeToken()).willReturn("reset-token");
        given(passwordResetStore.verifyCodeAndSaveResetToken("test@example.com", "123456", "reset-token"))
                .willReturn(VerifyCodeResult.VERIFY_ATTEMPT_EXCEEDED);

        assertThatThrownBy(() -> passwordResetService.verifyPasswordResetCode(
                new PasswordResetRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.VERIFY_ATTEMPT_EXCEEDED);
    }

    @Test
    void verifyPasswordResetCodeFailsWhenStoreThrowsUnexpectedException() {
        given(randomCodeGenerator.generateUrlSafeToken()).willReturn("reset-token");
        willThrow(new IllegalStateException("redis down"))
                .given(passwordResetStore)
                .verifyCodeAndSaveResetToken("test@example.com", "123456", "reset-token");

        assertThatThrownBy(() -> passwordResetService.verifyPasswordResetCode(
                new PasswordResetRequest.Verify("test@example.com", "123456")
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.STORE_FAILED);
    }

    private Member emailMember() {
        return Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("tester")
                .passwordHash("encoded-password")
                .build();
    }

    private Member socialOnlyMember() {
        return Member.builder()
                .id(2L)
                .email("social@example.com")
                .nickname("social")
                .passwordHash(null)
                .build();
    }
}

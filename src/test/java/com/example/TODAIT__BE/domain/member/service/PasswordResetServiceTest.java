package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.PasswordResetErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.PasswordResetRequest;
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
import com.example.TODAIT__BE.global.util.RandomCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
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
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordResetStore passwordResetStore;
    @Mock
    private PasswordResetSender passwordResetSender;
    @Mock
    private RandomCodeGenerator randomCodeGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                memberRepository,
                refreshTokenRepository,
                passwordResetStore,
                passwordResetSender,
                randomCodeGenerator,
                passwordEncoder
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
                .isEqualTo(PasswordResetErrorCode.CODE_NOT_FOUND);
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
                .isEqualTo(PasswordResetErrorCode.CODE_EXPIRED);
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

    @Test
    void setNewPasswordUpdatesPasswordAndRevokesRefreshTokens() {
        Member member = emailMember();
        RefreshToken refreshToken = refreshToken(member);
        given(passwordResetStore.claimResetToken("reset-token"))
                .willReturn(new ConsumeResetTokenResult(ConsumeResetTokenStatus.VALID, "test@example.com"));
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.encode("NewTodait1234!")).willReturn("encoded-new-password");
        given(refreshTokenRepository.findAllByMemberAndRevokedAtIsNull(member))
                .willReturn(List.of(refreshToken));

        passwordResetService.setNewPassword(
                new PasswordResetRequest.SetNewPassword(
                        "reset-token",
                        "NewTodait1234!",
                        "NewTodait1234!"
                )
        );

        assertThat(member.getPasswordHash()).isEqualTo("encoded-new-password");
        assertThat(refreshToken.isRevoked()).isTrue();
        verify(passwordResetStore).claimResetToken("reset-token");
        verify(passwordResetStore).consumeResetToken("reset-token");
    }

    @Test
    void setNewPasswordConsumesResetTokenAfterCommitWhenTransactionIsActive() {
        Member member = emailMember();
        given(passwordResetStore.claimResetToken("reset-token"))
                .willReturn(new ConsumeResetTokenResult(ConsumeResetTokenStatus.VALID, "test@example.com"));
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.encode("NewTodait1234!")).willReturn("encoded-new-password");
        given(refreshTokenRepository.findAllByMemberAndRevokedAtIsNull(member))
                .willReturn(List.of());

        TransactionSynchronizationManager.initSynchronization();
        try {
            passwordResetService.setNewPassword(
                    new PasswordResetRequest.SetNewPassword(
                            "reset-token",
                            "NewTodait1234!",
                            "NewTodait1234!"
                    )
            );

            verify(passwordResetStore, never()).consumeResetToken(anyString());
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit);
            verify(passwordResetStore).consumeResetToken("reset-token");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void setNewPasswordFailsWhenPasswordCheckMismatches() {
        assertThatThrownBy(() -> passwordResetService.setNewPassword(
                new PasswordResetRequest.SetNewPassword(
                        "reset-token",
                        "NewTodait1234!",
                        "OtherTodait1234!"
                )
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.NEW_PASSWORD_MISMATCH);

        verify(passwordResetStore, never()).claimResetToken(anyString());
        verify(passwordResetStore, never()).consumeResetToken(anyString());
    }

    @Test
    void setNewPasswordFailsWhenResetTokenIsInvalid() {
        given(passwordResetStore.claimResetToken("reset-token"))
                .willReturn(new ConsumeResetTokenResult(ConsumeResetTokenStatus.INVALID, null));

        assertThatThrownBy(() -> passwordResetService.setNewPassword(
                new PasswordResetRequest.SetNewPassword(
                        "reset-token",
                        "NewTodait1234!",
                        "NewTodait1234!"
                )
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.INVALID_RESET_TOKEN);

        verify(memberRepository, never()).findByEmail(anyString());
        verify(passwordResetStore, never()).consumeResetToken(anyString());
    }

    @Test
    void setNewPasswordFailsWhenResetTokenExpired() {
        given(passwordResetStore.claimResetToken("reset-token"))
                .willReturn(new ConsumeResetTokenResult(ConsumeResetTokenStatus.EXPIRED, null));

        assertThatThrownBy(() -> passwordResetService.setNewPassword(
                new PasswordResetRequest.SetNewPassword(
                        "reset-token",
                        "NewTodait1234!",
                        "NewTodait1234!"
                )
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.RESET_TOKEN_EXPIRED);

        verify(passwordResetStore, never()).consumeResetToken(anyString());
    }

    @Test
    void setNewPasswordDoesNotConsumeResetTokenWhenMemberLookupFails() {
        given(passwordResetStore.claimResetToken("reset-token"))
                .willReturn(new ConsumeResetTokenResult(ConsumeResetTokenStatus.VALID, "test@example.com"));
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.setNewPassword(
                new PasswordResetRequest.SetNewPassword(
                        "reset-token",
                        "NewTodait1234!",
                        "NewTodait1234!"
                )
        ))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(PasswordResetErrorCode.INVALID_RESET_TOKEN);

        verify(passwordResetStore, never()).consumeResetToken(anyString());
    }

    @Test
    void setNewPasswordFailsWhenStoreThrowsUnexpectedException() {
        willThrow(new IllegalStateException("redis down"))
                .given(passwordResetStore)
                .claimResetToken("reset-token");

        assertThatThrownBy(() -> passwordResetService.setNewPassword(
                new PasswordResetRequest.SetNewPassword(
                        "reset-token",
                        "NewTodait1234!",
                        "NewTodait1234!"
                )
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

    private RefreshToken refreshToken(Member member) {
        return RefreshToken.builder()
                .member(member)
                .tokenHash("refresh-token-hash")
                .expiresAt(LocalDateTime.now().plusHours(1))
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

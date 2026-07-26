package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.SignRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailLoginServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthService authService;
    @Mock
    private MemberLoginValidator memberLoginValidator;

    private EmailLoginService emailLoginService;

    @BeforeEach
    void setUp() {
        emailLoginService = new EmailLoginService(
                memberRepository,
                passwordEncoder,
                authService,
                memberLoginValidator
        );
    }

    @Test
    void loginNormalizesEmailAndIssuesTokens() {
        SignRequest.Login request = new SignRequest.Login(" User@Example.com ", "password!1");
        Member member = Member.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("user")
                .passwordHash("encoded-password")
                .status(MemberStatus.ACTIVE)
                .build();
        AuthTokenResponse.Token token = new AuthTokenResponse.Token("access", "refresh");

        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password!1", "encoded-password")).willReturn(true);
        given(authService.issueTokens(member)).willReturn(token);

        AuthTokenResponse.Token response = emailLoginService.login(request);

        assertThat(response).isEqualTo(token);
        verify(memberLoginValidator).validateLoginAvailable(member);
    }

    @Test
    void loginRejectsUnknownEmail() {
        SignRequest.Login request = new SignRequest.Login("unknown@example.com", "password!1");
        given(memberRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> emailLoginService.login(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }

    @Test
    void loginRejectsWrongPassword() {
        SignRequest.Login request = new SignRequest.Login("user@example.com", "wrong-password");
        Member member = Member.builder()
                .email("user@example.com")
                .nickname("user")
                .passwordHash("encoded-password")
                .build();

        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        assertThatThrownBy(() -> emailLoginService.login(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }

    @Test
    void loginRejectsSocialOnlyMember() {
        SignRequest.Login request = new SignRequest.Login("social@example.com", "password!1");
        Member member = Member.builder()
                .email("social@example.com")
                .nickname("social")
                .passwordHash(null)
                .build();

        given(memberRepository.findByEmail("social@example.com")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> emailLoginService.login(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }
}

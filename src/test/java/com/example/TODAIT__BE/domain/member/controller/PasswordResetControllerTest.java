package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.PasswordResetErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.PasswordResetRequest;
import com.example.TODAIT__BE.domain.member.dto.response.PasswordResetResponse;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PasswordResetController.class)
@Import(PasswordResetControllerTest.TestSecurityConfig.class)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PasswordResetService passwordResetService;

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Test
    void sendPasswordResetCode_success() throws Exception {
        given(passwordResetService.sendPasswordResetCode(any()))
                .willReturn(new PasswordResetResponse.Send());

        mockMvc.perform(post("/api/auth/password-reset/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Send("test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH200_4"))
                .andExpect(jsonPath("$.message").value("비밀번호 재설정 인증번호 발송 성공"))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void sendPasswordResetCode_unknownEmail_returnsSameSuccessResponse() throws Exception {
        given(passwordResetService.sendPasswordResetCode(any()))
                .willReturn(new PasswordResetResponse.Send());

        mockMvc.perform(post("/api/auth/password-reset/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Send("missing@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH200_4"))
                .andExpect(jsonPath("$.message").value("비밀번호 재설정 인증번호 발송 성공"))
                .andExpect(jsonPath("$.result").isMap())
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void sendPasswordResetCode_invalidRequest_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Send("invalid-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_1"));
    }

    @Test
    void verifyPasswordResetCode_success() throws Exception {
        given(passwordResetService.verifyPasswordResetCode(any()))
                .willReturn(new PasswordResetResponse.Verify("reset-token"));

        mockMvc.perform(post("/api/auth/password-reset/email/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Verify("test@example.com", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("AUTH200_5"))
                .andExpect(jsonPath("$.message").value("비밀번호 재설정 인증번호 확인 성공"))
                .andExpect(jsonPath("$.result.resetToken").value("reset-token"));
    }

    @Test
    void verifyPasswordResetCode_codeMismatch_returns400() throws Exception {
        given(passwordResetService.verifyPasswordResetCode(any()))
                .willThrow(new MemberException(PasswordResetErrorCode.CODE_MISMATCH));

        mockMvc.perform(post("/api/auth/password-reset/email/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Verify("test@example.com", "000000"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH400_4"))
                .andExpect(jsonPath("$.message").value("인증번호가 일치하지 않습니다."));
    }

    @Test
    void verifyPasswordResetCode_codeNotFound_returns400() throws Exception {
        given(passwordResetService.verifyPasswordResetCode(any()))
                .willThrow(new MemberException(PasswordResetErrorCode.CODE_MISMATCH));

        mockMvc.perform(post("/api/auth/password-reset/email/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Verify("missing@example.com", "123456"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH400_4"))
                .andExpect(jsonPath("$.message").value("인증번호가 일치하지 않습니다."));
    }

    @Test
    void verifyPasswordResetCode_codeExpired_returns400() throws Exception {
        given(passwordResetService.verifyPasswordResetCode(any()))
                .willThrow(new MemberException(PasswordResetErrorCode.CODE_MISMATCH));

        mockMvc.perform(post("/api/auth/password-reset/email/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Verify("test@example.com", "123456"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH400_4"))
                .andExpect(jsonPath("$.message").value("인증번호가 일치하지 않습니다."));
    }

    @Test
    void verifyPasswordResetCode_verifyAttemptExceeded_returns429() throws Exception {
        given(passwordResetService.verifyPasswordResetCode(any()))
                .willThrow(new MemberException(PasswordResetErrorCode.VERIFY_ATTEMPT_EXCEEDED));

        mockMvc.perform(post("/api/auth/password-reset/email/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Verify("test@example.com", "123456"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("AUTH429_2"))
                .andExpect(jsonPath("$.message").value("인증번호 확인 요청이 너무 많습니다."));
    }

    @Test
    void verifyPasswordResetCode_invalidRequest_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/email/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Verify("test@example.com", "abc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.message").value("인증번호는 6자리 숫자여야 합니다."));
    }
}

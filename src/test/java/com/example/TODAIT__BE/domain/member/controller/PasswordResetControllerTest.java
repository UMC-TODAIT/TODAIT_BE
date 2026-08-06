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
    void sendPasswordResetCode_memberNotFound_returns404() throws Exception {
        given(passwordResetService.sendPasswordResetCode(any()))
                .willThrow(new MemberException(PasswordResetErrorCode.EMAIL_NOT_FOUND));

        mockMvc.perform(post("/api/auth/password-reset/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Send("missing@example.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("AUTH404_1"))
                .andExpect(jsonPath("$.message").value("해당 이메일로 가입된 회원이 없습니다."));
    }

    @Test
    void sendPasswordResetCode_socialOnlyMember_returns400() throws Exception {
        given(passwordResetService.sendPasswordResetCode(any()))
                .willThrow(new MemberException(PasswordResetErrorCode.EMAIL_MEMBER_ONLY));

        mockMvc.perform(post("/api/auth/password-reset/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Send("social@example.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH400_2"))
                .andExpect(jsonPath("$.message").value("일반 이메일 회원만 비밀번호 재설정이 가능합니다."));
    }

    @Test
    void sendPasswordResetCode_resendCooldown_returns429() throws Exception {
        given(passwordResetService.sendPasswordResetCode(any()))
                .willThrow(new MemberException(PasswordResetErrorCode.RESEND_COOLDOWN));

        mockMvc.perform(post("/api/auth/password-reset/email/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PasswordResetRequest.Send("test@example.com"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("AUTH429_1"))
                .andExpect(jsonPath("$.message").value("인증번호 발송 요청이 너무 많습니다."));
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
}

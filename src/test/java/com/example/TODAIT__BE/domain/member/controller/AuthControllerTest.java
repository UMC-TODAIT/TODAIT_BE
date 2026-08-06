package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.service.AuthService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

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
    void signup_invalidPassword_returnsValidationMessage() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "gocleanwater",
                                  "email": "kjsjung1105@naver.com",
                                  "password": "password",
                                  "termAgreements": [
                                    {
                                      "termType": "SERVICE",
                                      "agreed": true
                                    },
                                    {
                                      "termType": "PRIVACY",
                                      "agreed": true
                                    },
                                    {
                                      "termType": "LOCATION",
                                      "agreed": false
                                    },
                                    {
                                      "termType": "MARKETING",
                                      "agreed": false
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"))
                .andExpect(jsonPath("$.message")
                        .value("비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다."));

        verify(authService, never()).signup(any());
    }
}

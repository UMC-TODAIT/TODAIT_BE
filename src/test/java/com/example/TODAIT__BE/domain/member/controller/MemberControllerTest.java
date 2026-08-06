package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.dto.response.MemberResponse;
import com.example.TODAIT__BE.domain.member.enums.MemberRole;
import com.example.TODAIT__BE.domain.member.service.MemberService;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class)
@Import(MemberControllerTest.TestSecurityConfig.class)
class MemberControllerTest {

    private static final Long MEMBER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

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
    void checkNicknameAvailability_success() throws Exception {
        given(memberService.checkNicknameAvailability("tester"))
                .willReturn(new MemberResponse.NicknameAvailability("tester", true));

        mockMvc.perform(get("/api/members/nickname-availability")
                        .param("nickname", "tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_4"))
                .andExpect(jsonPath("$.result.nickname").value("tester"))
                .andExpect(jsonPath("$.result.available").value(true));
    }

    @Test
    void checkNicknameAvailability_invalidNickname_returns400() throws Exception {
        mockMvc.perform(get("/api/members/nickname-availability")
                        .param("nickname", "!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_1"));

        verify(memberService, never()).checkNicknameAvailability("!");
    }

    @Test
    void getMyInfo_success() throws Exception {
        given(memberService.getMyInfo(MEMBER_ID))
                .willReturn(new MemberResponse.Me(
                        MEMBER_ID,
                        "tester@example.com",
                        "tester",
                        "https://example.com/profile.png",
                        3L
                ));

        mockMvc.perform(get("/api/members/me")
                        .with(authentication(authMemberToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("MEMBER200_3"))
                .andExpect(jsonPath("$.result.memberId").value(MEMBER_ID))
                .andExpect(jsonPath("$.result.email").value("tester@example.com"))
                .andExpect(jsonPath("$.result.nickname").value("tester"))
                .andExpect(jsonPath("$.result.profileImageUrl").value("https://example.com/profile.png"))
                .andExpect(jsonPath("$.result.savedCourseCount").value(3L));
    }

    @Test
    void getMyInfo_withoutPrincipal_returns401() throws Exception {
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON401_1"));

        verify(memberService, never()).getMyInfo(MEMBER_ID);
    }

    private UsernamePasswordAuthenticationToken authMemberToken() {
        AuthMember authMember = new AuthMember(MEMBER_ID, MemberRole.USER);
        return new UsernamePasswordAuthenticationToken(
                authMember,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + MemberRole.USER.name()))
        );
    }
}

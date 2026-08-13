package com.example.TODAIT__BE.domain.course.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.AbandonResponse;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.service.CourseDraftService;
import com.example.TODAIT__BE.domain.member.enums.MemberRole;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import java.time.LocalDateTime;
import java.util.List;
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

@WebMvcTest(controllers = CourseDraftController.class)
@Import(CourseDraftAbandonControllerTest.TestSecurityConfig.class)
class CourseDraftAbandonControllerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long COURSE_DRAFT_ID = 15L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseDraftService courseDraftService;

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }

    @Test
    void abandonCourseDraft_success() throws Exception {
        LocalDateTime expiresAt = LocalDateTime.of(2026, 9, 11, 12, 0);
        given(courseDraftService.abandonCourseDraft(eq(COURSE_DRAFT_ID), eq(MEMBER_ID)))
                .willReturn(new AbandonResponse(
                        COURSE_DRAFT_ID,
                        CourseDraftStatus.ABANDONED,
                        expiresAt
                ));

        mockMvc.perform(delete("/api/course-drafts/{courseDraftId}", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COURSE200_13"))
                .andExpect(jsonPath("$.message").value("임시 코스 포기 성공"))
                .andExpect(jsonPath("$.result.courseDraftId").value(COURSE_DRAFT_ID))
                .andExpect(jsonPath("$.result.draftStatus").value("ABANDONED"))
                .andExpect(jsonPath("$.result.expiresAt").exists());
    }

    @Test
    void abandonCourseDraft_terminalStatus_returns409() throws Exception {
        given(courseDraftService.abandonCourseDraft(eq(COURSE_DRAFT_ID), eq(MEMBER_ID)))
                .willThrow(new CourseException(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT));

        mockMvc.perform(delete("/api/course-drafts/{courseDraftId}", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT.getCode()));
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

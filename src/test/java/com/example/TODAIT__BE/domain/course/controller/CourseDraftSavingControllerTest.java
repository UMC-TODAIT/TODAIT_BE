package com.example.TODAIT__BE.domain.course.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftPlaceResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftSavingEnterResponse;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.service.CourseDraftBasePlaceService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftFoodCategoryService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftMoodTagService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftOrderingService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftPlaceService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftSavingService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftService;
import com.example.TODAIT__BE.domain.member.enums.MemberRole;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
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
@Import(CourseDraftSavingControllerTest.TestSecurityConfig.class)
class CourseDraftSavingControllerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long COURSE_DRAFT_ID = 15L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseDraftSavingService courseDraftSavingService;
    @MockitoBean
    private CourseDraftService courseDraftService;
    @MockitoBean
    private CourseDraftMoodTagService courseDraftMoodTagService;
    @MockitoBean
    private CourseDraftFoodCategoryService courseDraftFoodCategoryService;
    @MockitoBean
    private CourseDraftBasePlaceService courseDraftBasePlaceService;
    @MockitoBean
    private CourseDraftPlaceService courseDraftPlaceService;
    @MockitoBean
    private CourseDraftOrderingService courseDraftOrderingService;

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
    void enterSaving_success() throws Exception {
        CourseDraftSavingEnterResponse response = new CourseDraftSavingEnterResponse(
                COURSE_DRAFT_ID,
                CourseDraftStatus.SAVING,
                2,
                List.of(
                        new CourseDraftPlaceResponse(
                                100L,
                                1000L,
                                PlaceRole.BASE,
                                1,
                                "base",
                                "base address",
                                37.0,
                                127.0
                        ),
                        new CourseDraftPlaceResponse(
                                101L,
                                1001L,
                                PlaceRole.SELECTED,
                                2,
                                "selected",
                                "selected address",
                                37.1,
                                127.1
                        )
                )
        );
        given(courseDraftSavingService.enterSaving(eq(COURSE_DRAFT_ID), eq(MEMBER_ID)))
                .willReturn(response);

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/saving", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COURSE200_8"))
                .andExpect(jsonPath("$.message").value("임시 코스 저장 화면 진입 성공"))
                .andExpect(jsonPath("$.result.courseDraftId").value(COURSE_DRAFT_ID))
                .andExpect(jsonPath("$.result.draftStatus").value("SAVING"))
                .andExpect(jsonPath("$.result.totalPlaceCount").value(2))
                .andExpect(jsonPath("$.result.routePreview.length()").value(2))
                .andExpect(jsonPath("$.result.routePreview[0].placeRole").value("BASE"));
    }

    @Test
    void enterSaving_unsupportedDraftStatus_returns409() throws Exception {
        given(courseDraftSavingService.enterSaving(eq(COURSE_DRAFT_ID), eq(MEMBER_ID)))
                .willThrow(new CourseException(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT));

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/saving", COURSE_DRAFT_ID)
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

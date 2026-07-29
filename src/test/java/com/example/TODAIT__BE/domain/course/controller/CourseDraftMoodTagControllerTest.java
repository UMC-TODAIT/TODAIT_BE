package com.example.TODAIT__BE.domain.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftMoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftMoodTagSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftMoodTagSaveResponse.MoodTagItem;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.service.CourseDraftMoodTagService;
import com.example.TODAIT__BE.domain.member.enums.MemberRole;
import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomyErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CourseDraftMoodTagController.class)
@Import(CourseDraftMoodTagControllerTest.TestSecurityConfig.class)
class CourseDraftMoodTagControllerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long COURSE_DRAFT_ID = 15L;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CourseDraftMoodTagService courseDraftMoodTagService;

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
    void saveMoodTags_success() throws Exception {
        CourseDraftMoodTagSaveResponse response = new CourseDraftMoodTagSaveResponse(
                COURSE_DRAFT_ID,
                CourseDraftStatus.FOOD_SELECTING,
                List.of(
                        new MoodTagItem(1L, "HIP", "힙한"),
                        new MoodTagItem(4L, "ROMANTIC", "로맨틱")
                )
        );
        given(courseDraftMoodTagService.saveMoodTags(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willReturn(response);

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/mood-tags", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftMoodTagSaveRequest(List.of(1L, 4L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COURSE200_1"))
                .andExpect(jsonPath("$.message").value("임시 코스 분위기 선택 저장 성공"))
                .andExpect(jsonPath("$.result.courseDraftId").value(COURSE_DRAFT_ID))
                .andExpect(jsonPath("$.result.draftStatus").value("FOOD_SELECTING"))
                .andExpect(jsonPath("$.result.moodTags.length()").value(2))
                .andExpect(jsonPath("$.result.moodTags[0].moodTagId").value(1))
                .andExpect(jsonPath("$.result.moodTags[0].code").value("HIP"))
                .andExpect(jsonPath("$.result.moodTags[0].name").value("힙한"));
    }

    @Test
    void saveMoodTags_notOwner_returns403() throws Exception {
        given(courseDraftMoodTagService.saveMoodTags(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/mood-tags", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftMoodTagSaveRequest(List.of(1L, 4L)))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED.getCode()));
    }

    @Test
    void saveMoodTags_draftNotFound_returns404() throws Exception {
        given(courseDraftMoodTagService.saveMoodTags(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseErrorCode.COURSE_DRAFT_NOT_FOUND));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/mood-tags", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftMoodTagSaveRequest(List.of(1L, 4L)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(CourseErrorCode.COURSE_DRAFT_NOT_FOUND.getCode()));
    }

    @Test
    void saveMoodTags_unsupportedDraftStatus_returns409() throws Exception {
        given(courseDraftMoodTagService.saveMoodTags(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseErrorCode.MOOD_TAG_DRAFT_STATUS_CONFLICT));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/mood-tags", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftMoodTagSaveRequest(List.of(1L, 4L)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(CourseErrorCode.MOOD_TAG_DRAFT_STATUS_CONFLICT.getCode()))
                .andExpect(jsonPath("$.message").value("현재 임시 코스 상태에서는 분위기를 저장할 수 없습니다."));
    }

    @Test
    void saveMoodTags_lessThanMinimumCount_returns400() throws Exception {
        given(courseDraftMoodTagService.saveMoodTags(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseErrorCode.MOOD_TAG_MIN_COUNT_NOT_MET));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/mood-tags", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftMoodTagSaveRequest(List.of(1L)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CourseErrorCode.MOOD_TAG_MIN_COUNT_NOT_MET.getCode()));
    }

    @Test
    void saveMoodTags_duplicateTag_returns400() throws Exception {
        given(courseDraftMoodTagService.saveMoodTags(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseErrorCode.DUPLICATE_MOOD_TAG));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/mood-tags", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftMoodTagSaveRequest(List.of(1L, 1L)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CourseErrorCode.DUPLICATE_MOOD_TAG.getCode()));
    }

    @Test
    void saveMoodTags_moodTagNotFound_returns404() throws Exception {
        given(courseDraftMoodTagService.saveMoodTags(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new TaxonomyException(TaxonomyErrorCode.MOOD_TAG_NOT_FOUND));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/mood-tags", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftMoodTagSaveRequest(List.of(999L, 1L)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MOOD_TAG404"));
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

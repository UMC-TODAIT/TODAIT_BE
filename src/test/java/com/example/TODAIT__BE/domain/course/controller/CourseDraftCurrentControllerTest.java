package com.example.TODAIT__BE.domain.course.controller;

import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.AreaSummary;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CategorySummary;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CurrentDraftPlace;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CurrentResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.FoodCategoryItem;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.MoodTagItem;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
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
@Import(CourseDraftCurrentControllerTest.TestSecurityConfig.class)
class CourseDraftCurrentControllerTest {

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
    void getCurrentCourseDraft_success() throws Exception {
        given(courseDraftService.getCurrentCourseDraft(MEMBER_ID))
                .willReturn(new CurrentResponse(
                        COURSE_DRAFT_ID,
                        CourseDraftStatus.ORDERING,
                        LocalDateTime.of(2026, 8, 12, 10, 0),
                        LocalDateTime.of(2026, 8, 12, 11, 0),
                        List.of(new MoodTagItem(2L, "CALM", "차분한")),
                        List.of(new FoodCategoryItem(3L, "KOREAN", "한식")),
                        List.of(new CurrentDraftPlace(
                                100L,
                                20L,
                                PlaceRole.BASE,
                                1,
                                "연남 카페",
                                "서울 마포구",
                                "서울 마포구",
                                37.56,
                                126.92,
                                "https://example.com/image.jpg",
                                new AreaSummary(1L, "YEONNAM", "연남"),
                                new CategorySummary(2L, "CAFE", "카페"),
                                "디저트"
                        ))
                ));

        mockMvc.perform(get("/api/course-drafts/current")
                        .with(authentication(authMemberToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COURSE200_14"))
                .andExpect(jsonPath("$.message").value("진행 중인 임시 코스 조회 성공"))
                .andExpect(jsonPath("$.result.courseDraftId").value(COURSE_DRAFT_ID))
                .andExpect(jsonPath("$.result.draftStatus").value("ORDERING"))
                .andExpect(jsonPath("$.result.moodTags[0].moodTagId").value(2))
                .andExpect(jsonPath("$.result.foodCategories[0].foodCategoryId").value(3))
                .andExpect(jsonPath("$.result.places[0].placeRole").value("BASE"))
                .andExpect(jsonPath("$.result.places[0].area.code").value("YEONNAM"))
                .andExpect(jsonPath("$.result.places[0].category.code").value("CAFE"));
    }

    @Test
    void getCurrentCourseDraft_returnsNullResultWhenDraftDoesNotExist() throws Exception {
        given(courseDraftService.getCurrentCourseDraft(MEMBER_ID))
                .willReturn(null);

        mockMvc.perform(get("/api/course-drafts/current")
                        .with(authentication(authMemberToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COURSE200_14"))
                .andExpect(jsonPath("$.result").value(nullValue()));
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

package com.example.TODAIT__BE.domain.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.FoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.FoodCategorySaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.FoodCategoryItem;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
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
import com.example.TODAIT__BE.domain.taxonomy.code.FoodCategoryErrorCode;
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

@WebMvcTest(controllers = CourseDraftController.class)
@Import(CourseDraftFoodCategoryControllerTest.TestSecurityConfig.class)
class CourseDraftFoodCategoryControllerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long COURSE_DRAFT_ID = 15L;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CourseDraftFoodCategoryService courseDraftFoodCategoryService;
    @MockitoBean
    private CourseDraftService courseDraftService;
    @MockitoBean
    private CourseDraftMoodTagService courseDraftMoodTagService;
    @MockitoBean
    private CourseDraftBasePlaceService courseDraftBasePlaceService;
    @MockitoBean
    private CourseDraftPlaceService courseDraftPlaceService;
    @MockitoBean
    private CourseDraftOrderingService courseDraftOrderingService;
    @MockitoBean
    private CourseDraftSavingService courseDraftSavingService;

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
    void saveFoodCategories_success() throws Exception {
        FoodCategorySaveResponse response = new FoodCategorySaveResponse(
                COURSE_DRAFT_ID,
                CourseDraftStatus.BASE_PLACE_SELECTING,
                List.of(
                        new FoodCategoryItem(3L, "WESTERN", "양식"),
                        new FoodCategoryItem(6L, "DESSERT", "디저트")
                )
        );
        given(courseDraftFoodCategoryService.saveFoodCategories(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willReturn(response);

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/food-categories", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FoodCategorySaveRequest(List.of(3L, 6L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COURSE200_2"))
                .andExpect(jsonPath("$.message").value("임시 코스 음식 선택 저장 성공"))
                .andExpect(jsonPath("$.result.courseDraftId").value(COURSE_DRAFT_ID))
                .andExpect(jsonPath("$.result.draftStatus").value("BASE_PLACE_SELECTING"))
                .andExpect(jsonPath("$.result.foodCategories.length()").value(2))
                .andExpect(jsonPath("$.result.foodCategories[0].foodCategoryId").value(3))
                .andExpect(jsonPath("$.result.foodCategories[0].code").value("WESTERN"))
                .andExpect(jsonPath("$.result.foodCategories[0].name").value("양식"));
    }

    @Test
    void saveFoodCategories_notOwner_returns403() throws Exception {
        given(courseDraftFoodCategoryService.saveFoodCategories(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/food-categories", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FoodCategorySaveRequest(List.of(3L, 6L)))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED.getCode()));
    }

    @Test
    void saveFoodCategories_draftNotFound_returns404() throws Exception {
        given(courseDraftFoodCategoryService.saveFoodCategories(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/food-categories", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FoodCategorySaveRequest(List.of(3L, 6L)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND.getCode()));
    }

    @Test
    void saveFoodCategories_unsupportedDraftStatus_returns409() throws Exception {
        given(courseDraftFoodCategoryService.saveFoodCategories(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.FOOD_CATEGORY_DRAFT_STATUS_CONFLICT));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/food-categories", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FoodCategorySaveRequest(List.of(3L, 6L)))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.FOOD_CATEGORY_DRAFT_STATUS_CONFLICT.getCode()))
                .andExpect(jsonPath("$.message").value("현재 임시 코스 상태에서는 음식 카테고리를 저장할 수 없습니다."));
    }

    @Test
    void saveFoodCategories_emptyList_returns400() throws Exception {
        given(courseDraftFoodCategoryService.saveFoodCategories(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.INVALID_FOOD_CATEGORY_COUNT));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/food-categories", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FoodCategorySaveRequest(List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.INVALID_FOOD_CATEGORY_COUNT.getCode()));
    }

    @Test
    void saveFoodCategories_duplicateCategory_returns400() throws Exception {
        given(courseDraftFoodCategoryService.saveFoodCategories(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.DUPLICATE_FOOD_CATEGORY));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/food-categories", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FoodCategorySaveRequest(List.of(3L, 3L)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.DUPLICATE_FOOD_CATEGORY.getCode()));
    }

    @Test
    void saveFoodCategories_foodCategoryNotFound_returns404() throws Exception {
        given(courseDraftFoodCategoryService.saveFoodCategories(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new TaxonomyException(FoodCategoryErrorCode.FOOD_CATEGORY_NOT_FOUND));

        mockMvc.perform(put("/api/course-drafts/{courseDraftId}/food-categories", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new FoodCategorySaveRequest(List.of(999L, 3L)))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FOOD_CATEGORY404"));
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

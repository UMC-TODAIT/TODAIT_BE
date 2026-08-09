package com.example.TODAIT__BE.domain.course.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftBasePlaceSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftBasePlaceSaveRequest.ExternalPlace;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftBasePlaceSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftBasePlaceSaveResponse.AreaSummary;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftBasePlaceSaveResponse.BasePlace;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftBasePlaceSaveResponse.CategorySummary;
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
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.code.AreaErrorCode;
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
@Import(CourseDraftBasePlaceControllerTest.TestSecurityConfig.class)
class CourseDraftBasePlaceControllerTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long COURSE_DRAFT_ID = 15L;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CourseDraftBasePlaceService courseDraftBasePlaceService;
    @MockitoBean
    private CourseDraftService courseDraftService;
    @MockitoBean
    private CourseDraftMoodTagService courseDraftMoodTagService;
    @MockitoBean
    private CourseDraftFoodCategoryService courseDraftFoodCategoryService;
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
    void saveBasePlace_withPlaceId_success() throws Exception {
        BasePlace basePlace = new BasePlace(
                21L, "애몽", "서울 마포구 연남로3길 13", "서울 마포구 연남로3길 13", 37.561234, 126.923456,
                new AreaSummary(2L, "YEONNAM", "연남"),
                new CategorySummary(2L, "RESTAURANT", "식당"),
                "양식", "OPERATOR", false, 1, PlaceRole.BASE
        );
        CourseDraftBasePlaceSaveResponse response =
                CourseDraftBasePlaceSaveResponse.of(COURSE_DRAFT_ID, CourseDraftStatus.PLACE_SELECTING, basePlace);

        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willReturn(response);

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(21L, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COURSE200_6"))
                .andExpect(jsonPath("$.result.courseDraftId").value(COURSE_DRAFT_ID))
                .andExpect(jsonPath("$.result.draftStatus").value("PLACE_SELECTING"))
                .andExpect(jsonPath("$.result.basePlace.placeId").value(21))
                .andExpect(jsonPath("$.result.basePlace.sourceType").value("OPERATOR"))
                .andExpect(jsonPath("$.result.basePlace.isNewPlace").value(false))
                .andExpect(jsonPath("$.result.basePlace.visitOrder").value(1))
                .andExpect(jsonPath("$.result.basePlace.placeRole").value("BASE"));
    }

    @Test
    void saveBasePlace_withExternalPlace_success() throws Exception {
        BasePlace basePlace = new BasePlace(
                84L, "연남동 카페 투데잇", "서울 마포구 연남동 123-4", "서울 마포구 동교로 00길 12", 37.561234, 126.923456,
                new AreaSummary(2L, "YEONNAM", "연남"),
                new CategorySummary(1L, "CAFE", "카페"),
                "디저트 카페", "KAKAO", true, 1, PlaceRole.BASE
        );
        CourseDraftBasePlaceSaveResponse response =
                CourseDraftBasePlaceSaveResponse.of(COURSE_DRAFT_ID, CourseDraftStatus.PLACE_SELECTING, basePlace);

        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willReturn(response);

        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1234567890", "연남동 카페 투데잇", "서울 마포구 연남동 123-4", "서울 마포구 동교로 00길 12",
                37.561234, 126.923456, "YEONNAM", "CAFE", "디저트 카페", "02-1234-5678",
                "https://place.map.kakao.com/1234567890"
        );

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(null, externalPlace))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.basePlace.isNewPlace").value(true))
                .andExpect(jsonPath("$.result.basePlace.sourceType").value("KAKAO"));
    }

    @Test
    void saveBasePlace_notOwner_returns403() throws Exception {
        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED));

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(21L, null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED.getCode()));
    }

    @Test
    void saveBasePlace_draftNotFound_returns404() throws Exception {
        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND));

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(21L, null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND.getCode()));
    }

    @Test
    void saveBasePlace_wrongDraftStatus_returns409() throws Exception {
        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.BASE_PLACE_DRAFT_STATUS_CONFLICT));

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(21L, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.BASE_PLACE_DRAFT_STATUS_CONFLICT.getCode()));
    }

    @Test
    void saveBasePlace_bothPlaceIdAndExternalPlace_returns400() throws Exception {
        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.BASE_PLACE_SOURCE_CONFLICT));

        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1", "n", "a", null, 0.0, 0.0, "YEONNAM", "CAFE", null, null, null
        );

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(21L, externalPlace))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.BASE_PLACE_SOURCE_CONFLICT.getCode()));
    }

    @Test
    void saveBasePlace_neitherPlaceIdNorExternalPlace_returns400() throws Exception {
        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new CourseException(CourseDraftErrorCode.BASE_PLACE_SOURCE_MISSING));

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(CourseDraftErrorCode.BASE_PLACE_SOURCE_MISSING.getCode()));
    }

    @Test
    void saveBasePlace_placeNotFound_returns404() throws Exception {
        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new PlaceException(PlaceErrorCode.PLACE_NOT_FOUND));

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(999L, null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLACE404"));
    }

    @Test
    void saveBasePlace_placeNotAvailable_returns400() throws Exception {
        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new PlaceException(PlaceErrorCode.PLACE_NOT_AVAILABLE));

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(21L, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PLACE400"));
    }

    @Test
    void saveBasePlace_areaNotSupported_returns400() throws Exception {
        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new TaxonomyException(AreaErrorCode.AREA_NOT_SUPPORTED));

        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1", "n", "a", null, 0.0, 0.0, "UNKNOWN", "CAFE", null, null, null
        );

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(null, externalPlace))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AREA400"));
    }

    @Test
    void saveBasePlace_dataSourceNotFound_returns404() throws Exception {
        given(courseDraftBasePlaceService.saveBasePlace(eq(COURSE_DRAFT_ID), eq(MEMBER_ID), any()))
                .willThrow(new PlaceException(PlaceErrorCode.DATA_SOURCE_NOT_FOUND));

        ExternalPlace externalPlace = new ExternalPlace(
                "UNKNOWN_SOURCE", "1", "n", "a", null, 0.0, 0.0, "YEONNAM", "CAFE", null, null, null
        );

        mockMvc.perform(patch("/api/course-drafts/{courseDraftId}/base-place", COURSE_DRAFT_ID)
                        .with(authentication(authMemberToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseDraftBasePlaceSaveRequest(null, externalPlace))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLACE404_1"));
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

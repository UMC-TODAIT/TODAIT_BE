package com.example.TODAIT__BE.domain.place.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.TODAIT__BE.domain.member.enums.MemberRole;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailCategoryResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailFoodCategoryResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.enums.BusinessStatus;
import com.example.TODAIT__BE.domain.place.service.PlaceSearchService;
import com.example.TODAIT__BE.domain.place.service.PlaceService;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import java.math.BigDecimal;
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

@WebMvcTest(controllers = PlaceController.class)
@Import(PlaceControllerTest.TestSecurityConfig.class)
class PlaceControllerTest {

    private static final Long MEMBER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaceSearchService placeSearchService;

    @MockitoBean
    private PlaceService placeService;

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
    void searchPlaces_returnsPlaceSearchSuccessCode() throws Exception {
        PlaceSearchResponse.SearchResult response = new PlaceSearchResponse.SearchResult(
                "연남동 카페",
                1,
                List.of(new PlaceSearchResponse.PlaceItem(
                        "kakao-1",
                        1L,
                        "투데잇 카페",
                        "서울 마포구",
                        "서울 마포구 도로명",
                        BigDecimal.valueOf(37.0),
                        BigDecimal.valueOf(127.0),
                        "02-0000-0000",
                        "https://place.example",
                        new PlaceSearchResponse.AreaInfo(1L, "YEONNAM", "연남"),
                        new PlaceSearchResponse.CategoryInfo(1L, "CAFE", "카페"),
                        "디저트 카페",
                        true,
                        "https://img.example/main.jpg",
                        null,
                        true
                ))
        );
        given(placeSearchService.search(eq("연남동 카페"))).willReturn(response);

        mockMvc.perform(get("/api/places/search")
                        .param("query", "연남동 카페")
                        .with(authentication(authMemberToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("PLACE200"))
                .andExpect(jsonPath("$.message").value("기준 장소 검색 결과 조회 성공"))
                .andExpect(jsonPath("$.result.resultCount").value(1));
    }

    @Test
    void getPlaceDetail_returnsPlaceDetailSuccessCode() throws Exception {
        PlaceDetailResponse response = new PlaceDetailResponse(
                1L,
                "투데잇 카페",
                "서울 마포구",
                "서울 마포구 도로명",
                37.0,
                127.0,
                "02-0000-0000",
                "디저트 카페",
                "https://img.example/main.jpg",
                BusinessStatus.OPEN,
                "20:30",
                new PlaceDetailCategoryResponse(1L, "CAFE", "카페"),
                new PlaceDetailFoodCategoryResponse(1L, "DESSERT", "디저트"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "감성적인 분위기"
        );
        given(placeService.getPlaceDetail(1L)).willReturn(response);

        mockMvc.perform(get("/api/places/{placeId}", 1L)
                        .with(authentication(authMemberToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("PLACE200_1"))
                .andExpect(jsonPath("$.message").value("장소 상세 조회 성공"))
                .andExpect(jsonPath("$.result.placeId").value(1L));
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

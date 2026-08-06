package com.example.TODAIT__BE.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swagger() {
        Info info = new Info().title("Todait").description("Todait Swagger").version("0.0.1");

        // JWT 토큰 헤더 방식
        String securityScheme = "JWT TOKEN";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securityScheme);

        Components components = new Components()
                .addSecuritySchemes(securityScheme, new SecurityScheme()
                        .name(securityScheme)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(info)
                .addServersItem(new Server().url("/"))
                .addTagsItem(new Tag().name("MEMBER").description("로그인, 회원가입, 이메일 인증, 온보딩, 회원 정보 API"))
                .addTagsItem(new Tag().name("COURSE").description("임시 코스 및 저장 코스 API"))
                .addTagsItem(new Tag().name("PLACE").description("장소 검색 및 상세 조회 API"))
                .addTagsItem(new Tag().name("RECOMMENDATION").description("홈 추천 및 주변 핫플 추천 API"))
                .addTagsItem(new Tag().name("TAXONOMY").description("장소 카테고리 등 기준 정보 API"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}

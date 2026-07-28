package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.response.MemberMeResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(
        name = "Member",
        description = "회원 정보 관련 API"
)
public interface MemberControllerDocs {

    @Operation(
            summary = "내 회원 정보 조회",
            description = "현재 로그인한 사용자의 기본 정보와 마이페이지 요약 정보를 조회합니다."
    )
    ApiResponse<MemberMeResponse> getMyInfo(
            @AuthenticationPrincipal AuthMember authMember
    );
}

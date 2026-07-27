package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.response.MemberNicknameResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(
        name = "Member",
        description = "회원 정보 관련 API"
)
public interface MemberControllerDocs {

    @Operation(
            summary = "첫 메인화면 닉네임 조회",
            description = "현재 로그인한 사용자의 닉네임을 조회합니다."
    )
    ApiResponse<MemberNicknameResponse> getMyNickname(
            @AuthenticationPrincipal AuthMember authMember
    );
}

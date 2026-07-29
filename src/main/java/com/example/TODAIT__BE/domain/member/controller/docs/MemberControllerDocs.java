package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.response.MemberMeResponse;
import com.example.TODAIT__BE.domain.member.dto.response.NicknameAvailabilityResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(
        name = "Member",
        description = "회원 정보 관련 API"
)
public interface MemberControllerDocs {

    @Operation(
            summary = "Nickname availability check",
            description = "Checks whether the requested nickname is available."
    )
    ApiResponse<NicknameAvailabilityResponse> checkNicknameAvailability(
            @RequestParam
            @NotBlank
            @Size(min = 2, max = 12)
            @Pattern(regexp = "^[\\uAC00-\\uD7A3a-zA-Z0-9]+$")
            String nickname
    );

    @Operation(
            summary = "내 회원 정보 조회",
            description = "현재 로그인한 사용자의 기본 정보와 마이페이지 요약 정보를 조회합니다."
    )
    ApiResponse<MemberMeResponse> getMyInfo(
            @AuthenticationPrincipal AuthMember authMember
    );
}

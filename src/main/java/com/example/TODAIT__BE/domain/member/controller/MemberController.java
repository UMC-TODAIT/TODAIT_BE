package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.MemberSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.MemberControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.response.MemberNicknameResponse;
import com.example.TODAIT__BE.domain.member.service.MemberService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    @Override
    @GetMapping("/me/nickname")
    public ApiResponse<MemberNicknameResponse> getMyNickname(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        return ApiResponse.onSuccess(
                MemberSuccessCode.NICKNAME_RETRIEVED,
                memberService.getMyNickname(authMember.memberId())
        );
    }
}

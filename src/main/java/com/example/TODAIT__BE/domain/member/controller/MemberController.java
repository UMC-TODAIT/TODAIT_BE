package com.example.TODAIT__BE.domain.member.controller;

import com.example.TODAIT__BE.domain.member.code.MemberSuccessCode;
import com.example.TODAIT__BE.domain.member.controller.docs.MemberControllerDocs;
import com.example.TODAIT__BE.domain.member.dto.response.MemberResponse;
import com.example.TODAIT__BE.domain.member.service.MemberService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.apiPayload.code.GeneralErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    @Override
    @GetMapping("/nickname-availability")
    public ResponseEntity<ApiResponse<MemberResponse.NicknameAvailability>> checkNicknameAvailability(
            @RequestParam
            String nickname
    ) {
        MemberResponse.NicknameAvailability response =
                memberService.checkNicknameAvailability(nickname);
        return ResponseEntity
                .status(MemberSuccessCode.NICKNAME_AVAILABILITY_CHECKED.getStatus())
                .body(ApiResponse.onSuccess(
                        MemberSuccessCode.NICKNAME_AVAILABILITY_CHECKED,
                        response
                ));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse.Me>> getMyInfo(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        if (authMember == null) {
            throw new ProjectException(GeneralErrorCode.UNAUTHORIZED);
        }

        MemberResponse.Me response = memberService.getMyInfo(authMember.memberId());
        return ResponseEntity
                .status(MemberSuccessCode.MY_INFO_RETRIEVED.getStatus())
                .body(ApiResponse.onSuccess(
                        MemberSuccessCode.MY_INFO_RETRIEVED,
                        response
                ));
    }
}

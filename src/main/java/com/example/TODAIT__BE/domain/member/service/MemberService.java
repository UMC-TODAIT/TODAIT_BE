package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.response.MemberNicknameResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberNicknameResponse getMyNickname(Long memberId) {
        Member member = memberRepository
                .findByIdAndStatusAndDeletedAtIsNull(
                        memberId,
                        MemberStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new ProjectException(MemberErrorCode.MEMBER_NOT_FOUND)
                );

        return new MemberNicknameResponse(member.getNickname());
    }
}

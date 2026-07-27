package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.response.MemberNicknameResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
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
                .findById(memberId)
                .orElseThrow(() ->
                        new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
                );

        validateActiveMember(member);

        return new MemberNicknameResponse(member.getNickname());
    }

    private void validateActiveMember(Member member) {
        if (member.getDeletedAt() != null) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
    }
}

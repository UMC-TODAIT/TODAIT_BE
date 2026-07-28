package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;

import java.time.LocalDateTime;

final class MemberServiceTestFixtures {

    private MemberServiceTestFixtures() {
    }

    static Member activeMember(Long id) {
        return member(id, "member-" + id, MemberStatus.ACTIVE);
    }

    static Member member(Long id, String nickname, MemberStatus status) {
        return Member.builder()
                .id(id)
                .nickname(nickname)
                .status(status)
                .build();
    }

    static RefreshToken refreshToken(
            Member member,
            String tokenHash,
            LocalDateTime expiresAt
    ) {
        return RefreshToken.builder()
                .member(member)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();
    }
}

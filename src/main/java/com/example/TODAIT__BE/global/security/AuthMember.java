package com.example.TODAIT__BE.global.security;

import com.example.TODAIT__BE.domain.member.enums.MemberRole;

public record AuthMember(
        Long memberId,
        MemberRole role
) {
}

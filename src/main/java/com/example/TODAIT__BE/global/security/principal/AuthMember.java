package com.example.TODAIT__BE.global.security.principal;

import com.example.TODAIT__BE.domain.member.enums.MemberRole;

public record AuthMember(
        Long memberId,
        MemberRole role
) {
}

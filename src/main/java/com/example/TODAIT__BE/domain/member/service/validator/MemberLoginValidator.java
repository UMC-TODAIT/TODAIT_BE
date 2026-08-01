package com.example.TODAIT__BE.domain.member.service.validator;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import org.springframework.stereotype.Component;

@Component
public class MemberLoginValidator {

    public void validateLoginAvailable(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
    }
}

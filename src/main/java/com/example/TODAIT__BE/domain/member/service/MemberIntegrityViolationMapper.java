package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class MemberIntegrityViolationMapper {

    public RuntimeException mapMemberSaveException(
            DataIntegrityViolationException exception
    ) {
        if (hasConstraint(exception, "uk_member_email")) {
            return new MemberException(
                    MemberErrorCode.ALREADY_REGISTERED_EMAIL
            );
        }

        if (hasConstraint(exception, "uk_member_nickname")) {
            return new MemberException(
                    MemberErrorCode.ALREADY_REGISTERED_NICKNAME
            );
        }

        return exception;
    }

    public boolean hasConstraint(
            DataIntegrityViolationException exception,
            String expectedConstraint
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && expectedConstraint.equalsIgnoreCase(
                    violation.getConstraintName()
            )) {
                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}

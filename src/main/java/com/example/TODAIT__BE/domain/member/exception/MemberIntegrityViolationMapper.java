package com.example.TODAIT__BE.domain.member.exception;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
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
                    MemberErrorCode.ALREADY_REGISTERED_EMAIL,
                    exception
            );
        }

        if (hasConstraint(exception, "uk_member_nickname")) {
            return new MemberException(
                    MemberErrorCode.ALREADY_REGISTERED_NICKNAME,
                    exception
            );
        }

        return exception;
    }

    public RuntimeException mapOAuthAccountSaveException(
            DataIntegrityViolationException exception
    ) {
        if (hasConstraint(exception, "uk_member_oauth_provider_user")) {
            return new MemberException(
                    MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT,
                    exception
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

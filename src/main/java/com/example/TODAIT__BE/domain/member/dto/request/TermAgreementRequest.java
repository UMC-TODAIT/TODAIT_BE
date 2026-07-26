package com.example.TODAIT__BE.domain.member.dto.request;

import com.example.TODAIT__BE.domain.member.enums.TermType;
import jakarta.validation.constraints.NotNull;

public record TermAgreementRequest(
        @NotNull
        TermType termType,

        @NotNull
        Boolean agreed
) {
}

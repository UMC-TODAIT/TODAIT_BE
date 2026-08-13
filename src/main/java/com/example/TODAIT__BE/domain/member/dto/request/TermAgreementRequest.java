package com.example.TODAIT__BE.domain.member.dto.request;

import com.example.TODAIT__BE.domain.member.enums.TermType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "TermAgreementRequest",
        description = "약관 동의 정보"
)
public record TermAgreementRequest(
        @Schema(
                description = "약관 유형",
                example = "SERVICE"
        )
        @NotNull
        TermType termType,

        @Schema(
                description = "약관 동의 여부"
        )
        @NotNull
        Boolean agreed
) {
}

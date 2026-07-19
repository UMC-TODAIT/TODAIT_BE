package com.example.TODAIT__BE.domain.member.dto.request;

import com.example.TODAIT__BE.domain.member.enums.TermType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public class OAuthOnboardingRequest {

    public record Complete(
            @NotBlank
            @Size(min = 2, max = 12)
            @Pattern(
                    regexp = "^[가-힣a-zA-Z0-9]+$",
                    message = "닉네임에는 한글, 영문, 숫자만 사용할 수 있습니다."
            )
            String nickname,

            @NotEmpty
            List<@Valid TermAgreement> termAgreements
    ){}

    public record TermAgreement(
            @NotNull
            TermType termType,
            @NotNull
            Boolean agreed
    ){}

}

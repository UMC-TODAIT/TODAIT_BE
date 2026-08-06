package com.example.TODAIT__BE.domain.recommendation.code;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RecommendationErrorCodeTest {

    @Test
    void recommendationErrorCodesUseRecommendationPrefixAndAreUnique() {
        var codes = Arrays.stream(RecommendationErrorCode.values())
                .map(RecommendationErrorCode::getCode)
                .toList();

        assertThat(codes).allMatch(code -> code.startsWith("RECOMMENDATION"));
        assertThat(codes).hasSameSizeAs(codes.stream().collect(Collectors.toSet()));
    }
}

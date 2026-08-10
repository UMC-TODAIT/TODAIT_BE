package com.example.TODAIT__BE.domain.recommendation.code;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class RecommendationErrorCodeTest {

    @Test
    void recommendationErrorCodesUseRecommendationPrefixAndAreUnique() {
        var codes = Stream.of(
                        Arrays.stream(HomeRecommendationErrorCode.values()),
                        Arrays.stream(HotPlaceRecommendationErrorCode.values()),
                        Arrays.stream(RecommendedPlaceErrorCode.values()),
                        Arrays.stream(RecommendationLogErrorCode.values())
                )
                .flatMap(stream -> stream)
                .map(errorCode -> errorCode.getCode())
                .toList();

        assertThat(codes).allMatch(code -> code.startsWith("RECOMMENDATION"));
        assertThat(codes).hasSameSizeAs(codes.stream().collect(Collectors.toSet()));
    }
}

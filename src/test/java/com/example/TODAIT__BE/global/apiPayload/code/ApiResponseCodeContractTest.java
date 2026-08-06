package com.example.TODAIT__BE.global.apiPayload.code;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.code.AuthSuccessCode;
import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.domain.member.code.EmailVerificationSuccessCode;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.code.MemberSuccessCode;
import com.example.TODAIT__BE.domain.member.code.OAuthErrorCode;
import com.example.TODAIT__BE.domain.member.code.OAuthSuccessCode;
import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.code.PlaceSuccessCode;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationSuccessCode;
import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomyErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomySuccessCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ApiResponseCodeContractTest {

    private static final List<CodeEnum> CODE_ENUMS = List.of(
            new CodeEnum(GeneralSuccessCode.class, Set.of("COMMON")),
            new CodeEnum(GeneralErrorCode.class, Set.of("COMMON")),
            new CodeEnum(MemberSuccessCode.class, Set.of("MEMBER")),
            new CodeEnum(MemberErrorCode.class, Set.of("MEMBER", "AUTH")),
            new CodeEnum(AuthSuccessCode.class, Set.of("AUTH")),
            new CodeEnum(AuthErrorCode.class, Set.of("AUTH")),
            new CodeEnum(OAuthSuccessCode.class, Set.of("AUTH")),
            new CodeEnum(OAuthErrorCode.class, Set.of("AUTH")),
            new CodeEnum(EmailVerificationSuccessCode.class, Set.of("EMAIL")),
            new CodeEnum(EmailVerificationErrorCode.class, Set.of("EMAIL")),
            new CodeEnum(CourseSuccessCode.class, Set.of("COURSE")),
            new CodeEnum(CourseErrorCode.class, Set.of("COURSE")),
            new CodeEnum(PlaceSuccessCode.class, Set.of("PLACE")),
            new CodeEnum(PlaceErrorCode.class, Set.of("PLACE")),
            new CodeEnum(RecommendationSuccessCode.class, Set.of("RECOMMENDATION")),
            new CodeEnum(RecommendationErrorCode.class, Set.of("RECOMMENDATION")),
            new CodeEnum(TaxonomySuccessCode.class, Set.of("TAXONOMY")),
            new CodeEnum(TaxonomyErrorCode.class, Set.of("TAXONOMY", "MOOD_TAG", "FOOD_CATEGORY"))
    );

    @Test
    void apiResponseCodesAreGloballyUnique() {
        Map<String, List<ResponseCode>> codesByValue = responseCodes().stream()
                .collect(Collectors.groupingBy(ResponseCode::code));

        assertThat(codesByValue).allSatisfy((code, owners) ->
                assertThat(owners)
                        .as("response code '%s' is used by %s", code, owners)
                        .hasSize(1)
        );
    }

    @Test
    void apiResponseCodesUsePrefixesAllowedForTheirEnums() {
        CODE_ENUMS.forEach(codeEnum -> {
            List<ResponseCode> invalidCodes = responseCodes(codeEnum).stream()
                    .filter(responseCode -> codeEnum.allowedPrefixes().stream()
                            .noneMatch(responseCode.code()::startsWith))
                    .toList();

            assertThat(invalidCodes)
                    .as("%s may only use prefixes %s", codeEnum.type().getSimpleName(), codeEnum.allowedPrefixes())
                    .isEmpty();
        });
    }

    private static List<ResponseCode> responseCodes() {
        return CODE_ENUMS.stream()
                .flatMap(codeEnum -> responseCodes(codeEnum).stream())
                .toList();
    }

    private static List<ResponseCode> responseCodes(CodeEnum codeEnum) {
        List<ResponseCode> responseCodes = new ArrayList<>();

        for (Enum<?> constant : codeEnum.type().getEnumConstants()) {
            if (constant instanceof BaseSuccessCode successCode) {
                responseCodes.add(ResponseCode.from(constant, successCode.getCode()));
            }
            if (constant instanceof BaseErrorCode errorCode) {
                responseCodes.add(ResponseCode.from(constant, errorCode.getCode()));
            }
        }

        return responseCodes;
    }

    private record CodeEnum(Class<? extends Enum<?>> type, Set<String> allowedPrefixes) {
    }

    private record ResponseCode(String owner, String code) {

        private static ResponseCode from(Enum<?> constant, String code) {
            return new ResponseCode(
                    constant.getDeclaringClass().getSimpleName() + "." + constant.name(),
                    code
            );
        }
    }
}

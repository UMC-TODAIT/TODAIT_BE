package com.example.TODAIT__BE.global.apiPayload.code;

import static java.util.Map.entry;
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
import com.example.TODAIT__BE.domain.member.code.PasswordResetErrorCode;
import com.example.TODAIT__BE.domain.member.code.PasswordResetSuccessCode;
import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.code.PlaceSuccessCode;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationSuccessCode;
import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomyErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomySuccessCode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

class ApiResponseCodeContractTest {

    private static final String ROOT_PACKAGE = "com.example.TODAIT__BE";

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
            new CodeEnum(PasswordResetSuccessCode.class, Set.of("AUTH")),
            new CodeEnum(PasswordResetErrorCode.class, Set.of("AUTH")),
            new CodeEnum(CourseSuccessCode.class, Set.of("COURSE")),
            new CodeEnum(CourseErrorCode.class, Set.of("COURSE")),
            new CodeEnum(PlaceSuccessCode.class, Set.of("PLACE")),
            new CodeEnum(PlaceErrorCode.class, Set.of("PLACE")),
            new CodeEnum(RecommendationSuccessCode.class, Set.of("RECOMMENDATION")),
            new CodeEnum(RecommendationErrorCode.class, Set.of("RECOMMENDATION")),
            new CodeEnum(TaxonomySuccessCode.class, Set.of("TAXONOMY")),
            new CodeEnum(TaxonomyErrorCode.class, Set.of("TAXONOMY", "MOOD_TAG", "FOOD_CATEGORY", "AREA", "PLACE_CATEGORY"))
    );

    private static final Map<String, String> EXPECTED_CODES = Map.ofEntries(
            entry("GeneralSuccessCode.OK", "COMMON200_1"),
            entry("GeneralErrorCode.BAD_REQUEST", "COMMON400_1"),
            entry("GeneralErrorCode.UNAUTHORIZED", "COMMON401_1"),
            entry("GeneralErrorCode.FORBIDDEN", "COMMON403_1"),
            entry("GeneralErrorCode.NOT_FOUND", "COMMON404_1"),
            entry("GeneralErrorCode.INTERNAL_SERVER_ERROR", "COMMON500_1"),
            entry("MemberSuccessCode.ONBOARDING_COMPLETED", "MEMBER200_1"),
            entry("MemberSuccessCode.SIGNUP_COMPLETED", "MEMBER201_1"),
            entry("MemberSuccessCode.LOGIN_COMPLETED", "MEMBER200_2"),
            entry("MemberSuccessCode.NICKNAME_AVAILABILITY_CHECKED", "MEMBER200_4"),
            entry("MemberSuccessCode.MY_INFO_RETRIEVED", "MEMBER200_3"),
            entry("MemberErrorCode.ALREADY_REGISTERED_EMAIL", "MEMBER409_1"),
            entry("MemberErrorCode.ALREADY_REGISTERED_NICKNAME", "MEMBER409_2"),
            entry("MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT", "MEMBER409_3"),
            entry("MemberErrorCode.INVALID_MEMBER_STATUS", "MEMBER403_1"),
            entry("MemberErrorCode.INVALID_ONBOARDING_TOKEN", "MEMBER401_1"),
            entry("MemberErrorCode.DUPLICATE_TERM_AGREEMENT", "MEMBER400_1"),
            entry("MemberErrorCode.REQUIRED_TERM_NOT_AGREED", "MEMBER400_2"),
            entry("MemberErrorCode.INVALID_TERM", "MEMBER400_3"),
            entry("MemberErrorCode.EMAIL_VERIFICATION_REQUIRED", "MEMBER400_4"),
            entry("MemberErrorCode.INVALID_EMAIL_OR_PASSWORD", "AUTH401_2"),
            entry("MemberErrorCode.MEMBER_NOT_FOUND", "MEMBER404_1"),
            entry("AuthSuccessCode.TOKEN_REFRESHED", "AUTH200_2"),
            entry("AuthSuccessCode.LOGOUT_COMPLETED", "AUTH200_3"),
            entry("AuthErrorCode.INVALID_REFRESH_TOKEN", "AUTH401_1"),
            entry("AuthErrorCode.REVOKED_REFRESH_TOKEN", "AUTH403_1"),
            entry("AuthErrorCode.EXPIRED_REFRESH_TOKEN", "AUTH410_1"),
            entry("OAuthSuccessCode.OAUTH_LOGIN_OK", "AUTH200_1"),
            entry("OAuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN", "AUTH401_3"),
            entry("OAuthErrorCode.KAKAO_USER_INFO_REQUEST_FAILED", "AUTH502_2"),
            entry("OAuthErrorCode.INVALID_GOOGLE_ID_TOKEN", "AUTH400_1"),
            entry("OAuthErrorCode.GOOGLE_ID_TOKEN_VERIFICATION_FAILED", "AUTH502_1"),
            entry("EmailVerificationSuccessCode.CODE_SENT", "EMAIL200_1"),
            entry("EmailVerificationSuccessCode.COMPLETED", "EMAIL200_2"),
            entry("EmailVerificationErrorCode.INVALID_EMAIL_FORMAT", "EMAIL400_1"),
            entry("EmailVerificationErrorCode.CODE_MISMATCH", "EMAIL400_2"),
            entry("EmailVerificationErrorCode.CODE_NOT_FOUND", "EMAIL400_3"),
            entry("EmailVerificationErrorCode.ALREADY_COMPLETED", "EMAIL400_4"),
            entry("EmailVerificationErrorCode.RESEND_COOLDOWN", "EMAIL429_1"),
            entry("EmailVerificationErrorCode.VERIFY_ATTEMPT_EXCEEDED", "EMAIL429_2"),
            entry("EmailVerificationErrorCode.SEND_FAILED", "EMAIL500_1"),
            entry("EmailVerificationErrorCode.STORE_FAILED", "EMAIL500_2"),
            entry("PasswordResetSuccessCode.CODE_SENT", "AUTH200_4"),
            entry("PasswordResetSuccessCode.CODE_VERIFIED", "AUTH200_5"),
            entry("PasswordResetSuccessCode.PASSWORD_UPDATED", "AUTH200_6"),
            entry("PasswordResetErrorCode.INVALID_EMAIL_FORMAT", "AUTH400_3"),
            entry("PasswordResetErrorCode.CODE_MISMATCH", "AUTH400_4"),
            entry("PasswordResetErrorCode.NEW_PASSWORD_MISMATCH", "AUTH400_5"),
            entry("PasswordResetErrorCode.EMAIL_MEMBER_ONLY", "AUTH400_2"),
            entry("PasswordResetErrorCode.EMAIL_NOT_FOUND", "AUTH404_1"),
            entry("PasswordResetErrorCode.CODE_NOT_FOUND", "AUTH404_2"),
            entry("PasswordResetErrorCode.INVALID_RESET_TOKEN", "AUTH401_4"),
            entry("PasswordResetErrorCode.CODE_EXPIRED", "AUTH410_2"),
            entry("PasswordResetErrorCode.RESET_TOKEN_EXPIRED", "AUTH410_3"),
            entry("PasswordResetErrorCode.RESEND_COOLDOWN", "AUTH429_1"),
            entry("PasswordResetErrorCode.VERIFY_ATTEMPT_EXCEEDED", "AUTH429_2"),
            entry("PasswordResetErrorCode.SEND_FAILED", "AUTH500_1"),
            entry("PasswordResetErrorCode.STORE_FAILED", "AUTH500_2"),
            entry("CourseSuccessCode.COURSE_DRAFT_CREATE_OK", "COURSE201"),
            entry("CourseSuccessCode.MOOD_TAG_SAVE_OK", "COURSE200_1"),
            entry("CourseSuccessCode.FOOD_CATEGORY_SAVE_OK", "COURSE200_2"),
            entry("CourseSuccessCode.COURSE_SAVE_OK", "COURSE_SAVE201"),
            entry("CourseSuccessCode.PLACE_ORDER_UPDATE_OK", "COURSE200"),
            entry("CourseSuccessCode.ORDERING_ENTRY_OK", "COURSE200_9"),
            entry("CourseSuccessCode.RECOMMENDED_COURSE_DETAIL_OK", "COURSE200_3"),
            entry("CourseSuccessCode.SAVED_COURSE_OVERVIEW_OK", "COURSE200_4"),
            entry("CourseSuccessCode.SAVED_COURSE_DETAIL_OK", "COURSE200_5"),
            entry("CourseSuccessCode.RECOMMENDED_COURSE_SAVE_OK", "COURSE202"),
            entry("CourseSuccessCode.COURSE_DRAFT_SAVING_ENTER_OK", "COURSE200_8"),
            entry("CourseErrorCode.COURSE_DRAFT_NOT_FOUND", "COURSE_DRAFT404"),
            entry("CourseErrorCode.COURSE_DRAFT_ACCESS_DENIED", "COURSE403_1"),
            entry("CourseErrorCode.COURSE_DRAFT_ALREADY_COMPLETED", "COURSE_DRAFT_COMPLETED409"),
            entry("CourseErrorCode.INVALID_COURSE_TITLE", "COURSE_TITLE400"),
            entry("CourseErrorCode.INVALID_MOOD_TAG_COUNT", "COURSE_MOOD400"),
            entry("CourseErrorCode.DUPLICATE_MOOD_TAG", "COURSE400_2"),
            entry("CourseErrorCode.MOOD_TAG_DRAFT_STATUS_CONFLICT", "COURSE_MOOD409"),
            entry("CourseErrorCode.FOOD_CATEGORY_NOT_SELECTED", "COURSE_FOOD400"),
            entry("CourseErrorCode.INVALID_FOOD_CATEGORY_COUNT", "COURSE400_3"),
            entry("CourseErrorCode.DUPLICATE_FOOD_CATEGORY", "COURSE400_4"),
            entry("CourseErrorCode.FOOD_CATEGORY_DRAFT_STATUS_CONFLICT", "COURSE_FOOD409"),
            entry("CourseErrorCode.INVALID_BASE_PLACE", "COURSE_BASE400"),
            entry("CourseErrorCode.INVALID_SELECTED_PLACE", "COURSE_PLACE400"),
            entry("CourseErrorCode.COURSE_DRAFT_STATUS_CONFLICT", "COURSE_DRAFT409"),
            entry("CourseErrorCode.COURSE_DRAFT_BASE_PLACE_CONFLICT", "COURSE_BASE409"),
            entry("CourseErrorCode.COURSE_DRAFT_SELECTED_PLACE_CONFLICT", "COURSE_PLACE409"),
            entry("CourseErrorCode.SELECTED_PLACE_NOT_FOUND", "COURSE_PLACE404"),
            entry("CourseErrorCode.BASE_PLACE_NOT_REORDERABLE", "COURSE_BASE_REORDER400"),
            entry("CourseErrorCode.INVALID_VISIT_ORDER", "COURSE_ORDER400"),
            entry("CourseErrorCode.PLACE_ORDER_DRAFT_STATUS_CONFLICT", "COURSE_ORDER409"),
            entry("CourseErrorCode.ORDERING_ENTRY_STATUS_CONFLICT", "COURSE409"),
            entry("CourseErrorCode.ORDERING_ENTRY_INVALID_BASE_PLACE", "COURSE_BASE409_1"),
            entry("CourseErrorCode.ORDERING_ENTRY_SELECTED_PLACE_REQUIRED", "COURSE_PLACE409_1"),
            entry("CourseErrorCode.ORDERING_ENTRY_INVALID_VISIT_ORDER", "COURSE_PLACE409_2"),
            entry("CourseErrorCode.RECOMMENDED_COURSE_NOT_FOUND", "COURSE404"),
            entry("CourseErrorCode.SAVED_COURSE_ACCESS_DENIED", "COURSE403_2"),
            entry("CourseErrorCode.SAVED_COURSE_NOT_FOUND", "COURSE404_1"),
            entry("CourseErrorCode.RECOMMENDED_COURSE_NOT_SAVABLE", "COURSE400_5"),
            entry("CourseErrorCode.BASE_PLACE_DRAFT_STATUS_CONFLICT", "COURSE_BASE409_2"),
            entry("CourseErrorCode.BASE_PLACE_SOURCE_CONFLICT", "COURSE400_6"),
            entry("CourseErrorCode.BASE_PLACE_SOURCE_MISSING", "COURSE400_7"),
            entry("CourseErrorCode.COURSE_MOOD_TAG_NOT_FOUND", "COURSE_MOOD404"),
            entry("CourseSuccessCode.BASE_PLACE_SAVE_OK", "COURSE200_6"),
            entry("PlaceSuccessCode.PLACE_SEARCH_OK", "PLACE200"),
            entry("PlaceSuccessCode.PLACE_DETAIL_OK", "PLACE200_1"),
            entry("PlaceErrorCode.INVALID_PLACE_SEARCH_QUERY", "PLACE400_1"),
            entry("PlaceErrorCode.PLACE_SEARCH_QUERY_TOO_SHORT", "PLACE400_2"),
            entry("PlaceErrorCode.PLACE_SEARCH_QUERY_TOO_LONG", "PLACE400_3"),
            entry("PlaceErrorCode.PLACE_NOT_EXPOSED", "PLACE400_4"),
            entry("PlaceErrorCode.PLACE_NOT_FOUND", "PLACE404"),
            entry("PlaceErrorCode.KAKAO_LOCAL_API_RATE_LIMIT_EXCEEDED", "PLACE429_1"),
            entry("PlaceErrorCode.KAKAO_LOCAL_API_REQUEST_FAILED", "PLACE502_1"),
            entry("PlaceErrorCode.PLACE_NOT_AVAILABLE", "PLACE400"),
            entry("PlaceErrorCode.INVALID_PLACE_COORDINATE", "PLACE400_5"),
            entry("PlaceErrorCode.DATA_SOURCE_NOT_FOUND", "PLACE404_1"),
            entry("RecommendationSuccessCode.HOME_RECOMMENDED_COURSE_LIST_OK", "RECOMMENDATION200"),
            entry("RecommendationSuccessCode.HOT_PLACE_LIST_OK", "RECOMMENDATION200_2"),
            entry("RecommendationSuccessCode.HOME_RECOMMENDED_PLACE_LIST_OK", "RECOMMENDATION201"),
            entry("RecommendationErrorCode.INVALID_PAGE", "RECOMMENDATION400_1"),
            entry("RecommendationErrorCode.INVALID_SIZE", "RECOMMENDATION400_2"),
            entry("RecommendationErrorCode.INVALID_HOT_PLACE_SIZE", "RECOMMENDATION400_3"),
            entry("RecommendationErrorCode.INCOMPLETE_COORDINATES", "RECOMMENDATION400_4"),
            entry("RecommendationErrorCode.INVALID_COORDINATES", "RECOMMENDATION400_5"),
            entry("RecommendationErrorCode.INVALID_PLACE_SIZE", "RECOMMENDATION400_6"),
            entry("RecommendationErrorCode.INVALID_LOCATION_PAIR", "RECOMMENDATION400_7"),
            entry("RecommendationErrorCode.INVALID_LOCATION_RANGE", "RECOMMENDATION400_8"),
            entry("RecommendationErrorCode.INVALID_CURSOR", "RECOMMENDATION400_9"),
            entry("RecommendationErrorCode.REQUEST_CONTEXT_SERIALIZATION_FAILED", "RECOMMENDATION500_1"),
            entry("TaxonomySuccessCode.PLACE_CATEGORY_LIST_OK", "TAXONOMY200"),
            entry("TaxonomyErrorCode.MOOD_TAG_NOT_FOUND", "MOOD_TAG404"),
            entry("TaxonomyErrorCode.FOOD_CATEGORY_NOT_FOUND", "FOOD_CATEGORY404"),
            entry("TaxonomyErrorCode.AREA_NOT_SUPPORTED", "AREA400"),
            entry("TaxonomyErrorCode.PLACE_CATEGORY_NOT_SUPPORTED", "PLACE_CATEGORY400")
    );

    @Test
    void registeredCodeEnumsIncludeEveryBaseCodeEnumOnClasspath() {
        Set<Class<?>> registeredTypes = CODE_ENUMS.stream()
                .map(CodeEnum::type)
                .collect(Collectors.toSet());

        assertThat(registeredTypes).containsExactlyInAnyOrderElementsOf(discoverBaseCodeEnumTypes());
    }

    @Test
    void apiResponseCodesMatchExpectedSnapshot() {
        Map<String, String> actualCodes = responseCodes().stream()
                .collect(Collectors.toMap(ResponseCode::owner, ResponseCode::code));

        assertThat(actualCodes).containsExactlyInAnyOrderEntriesOf(EXPECTED_CODES);
    }

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

    private static Set<Class<?>> discoverBaseCodeEnumTypes() {
        Set<Class<?>> codeTypes = new HashSet<>();
        collectBaseCodeEnumTypes(BaseSuccessCode.class, codeTypes);
        collectBaseCodeEnumTypes(BaseErrorCode.class, codeTypes);
        return codeTypes;
    }

    private static void collectBaseCodeEnumTypes(Class<?> baseCodeType, Set<Class<?>> codeTypes) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(baseCodeType));

        scanner.findCandidateComponents(ROOT_PACKAGE).forEach(beanDefinition -> {
            Class<?> codeType = loadClass(beanDefinition.getBeanClassName());
            if (codeType.isEnum()) {
                codeTypes.add(codeType);
            }
        });
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Failed to load response code type: " + className, exception);
        }
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

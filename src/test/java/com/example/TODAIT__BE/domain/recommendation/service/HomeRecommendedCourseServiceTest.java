package com.example.TODAIT__BE.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedCourseListResponse;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedCourseResponse;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationResultRepository;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeRecommendedCourseServiceTest {

    private static final Long MEMBER_ID = 1L;

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CoursePlaceRepository coursePlaceRepository;
    @Mock
    private CourseMoodTagRepository courseMoodTagRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RecommendationLogRepository recommendationLogRepository;
    @Mock
    private RecommendationResultRepository recommendationResultRepository;

    private HomeRecommendedCourseService service;

    @BeforeEach
    void setUp() {
        service = new HomeRecommendedCourseService(
                courseRepository,
                coursePlaceRepository,
                courseMoodTagRepository,
                memberRepository,
                recommendationLogRepository,
                recommendationResultRepository,
                new ObjectMapper()
        );

        given(memberRepository.findById(MEMBER_ID))
                .willReturn(Optional.of(mock(Member.class)));
        given(recommendationLogRepository.save(any(RecommendationLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void returnsOneCoursePerAreaWithSequentialRanksAndSavesLogAndResults() {
        Course hongdae = course(1L, "HONGDAE", 10L, "홍대", 1, "홍대 코스");
        Course yeonnam = course(2L, "YEONNAM", 20L, "연남", 1, "연남 코스");
        Course seongsu = course(3L, "SEONGSU", 30L, "성수", 1, "성수 코스");

        // 중첩 given() 방지: mock 컬렉션을 먼저 구성한 뒤 스텁에 전달한다.
        List<Course> candidates = List.of(hongdae, yeonnam, seongsu);
        List<CoursePlace> coursePlaces = List.of(
                representativePlace(hongdae, "칵테일바", "https://img/1.jpg"),
                representativePlace(yeonnam, "브런치", "https://img/2.jpg"),
                representativePlace(seongsu, "전시", "https://img/3.jpg")
        );
        List<CourseMoodTag> moodTags = List.of(
                moodTag(hongdae, "HIP", "힙한"),
                moodTag(yeonnam, "ROMANTIC", "로맨틱"),
                moodTag(seongsu, "MODERN", "모던한")
        );

        given(courseRepository.findRecommendedCourseCandidates(
                CourseVisibility.RECOMMENDED,
                CourseSourceType.SERVICE_CREATED,
                List.of("HONGDAE", "YEONNAM", "SEONGSU")
        )).willReturn(candidates);
        given(coursePlaceRepository.findAllWithCourseAndPlaceByCourseIds(any()))
                .willReturn(coursePlaces);
        given(courseMoodTagRepository.findAllWithCourseAndMoodTagByCourseIds(any()))
                .willReturn(moodTags);

        HomeRecommendedCourseListResponse response =
                service.getHomeRecommendedCourses(MEMBER_ID, null, null);

        assertThat(response.size()).isEqualTo(3);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.courses()).hasSize(3);

        // 지역 순서는 날짜 로테이션에 따라 달라지므로 집합/구조 불변식으로 검증한다.
        assertThat(response.courses())
                .extracting(HomeRecommendedCourseResponse::courseId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(response.courses())
                .extracting(HomeRecommendedCourseResponse::rank)
                .containsExactly(1, 2, 3);
        assertThat(response.courses())
                .allSatisfy(course -> {
                    assertThat(course.detailAvailable()).isTrue();
                    assertThat(course.placeCount()).isEqualTo(1);
                    assertThat(course.tags()).hasSize(2);
                    assertThat(course.tags().get(0).type()).isEqualTo("MOOD");
                    assertThat(course.tags().get(1).type()).isEqualTo("SUB_CATEGORY");
                    assertThat(course.representativeImageUrl()).isNotNull();
                });

        verify(recommendationLogRepository).save(any(RecommendationLog.class));

        ArgumentCaptor<List<RecommendationResult>> resultsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(recommendationResultRepository).saveAll(resultsCaptor.capture());
        List<RecommendationResult> savedResults = resultsCaptor.getValue();
        assertThat(savedResults).hasSize(3);
        assertThat(savedResults)
                .extracting(RecommendationResult::getRankNo)
                .containsExactly(1, 2, 3);
        // XOR 규칙: 코스 추천 결과는 course_id만 존재하고 place_id는 null
        assertThat(savedResults)
                .allSatisfy(result -> {
                    assertThat(result.getCourse()).isNotNull();
                    assertThat(result.getPlace()).isNull();
                });
    }

    @Test
    void balancesAreasFirstWhenSizeExceedsThree() {
        List<Course> candidates = new ArrayList<>();
        List<CoursePlace> coursePlaces = new ArrayList<>();
        long id = 1L;
        for (String areaCode : List.of("HONGDAE", "YEONNAM", "SEONGSU")) {
            for (int priority = 1; priority <= 2; priority++) {
                Course course = course(
                        id, areaCode, id * 10, areaCode, priority,
                        areaCode + priority
                );
                candidates.add(course);
                coursePlaces.add(representativePlace(course, "sub", "img"));
                id++;
            }
        }

        given(courseRepository.findRecommendedCourseCandidates(
                any(), any(), any()
        )).willReturn(candidates);
        given(coursePlaceRepository.findAllWithCourseAndPlaceByCourseIds(any()))
                .willReturn(coursePlaces);
        given(courseMoodTagRepository.findAllWithCourseAndMoodTagByCourseIds(any()))
                .willReturn(List.of());

        HomeRecommendedCourseListResponse response =
                service.getHomeRecommendedCourses(MEMBER_ID, null, 6);

        assertThat(response.courses()).hasSize(6);
        assertThat(response.courses())
                .extracting(HomeRecommendedCourseResponse::rank)
                .containsExactly(1, 2, 3, 4, 5, 6);

        // 각 지역 1개씩 우선 배치: 앞 3개와 뒤 3개 모두 서로 다른 3개 지역으로 구성
        List<String> firstRound = response.courses().subList(0, 3).stream()
                .map(course -> course.area().code())
                .toList();
        List<String> secondRound = response.courses().subList(3, 6).stream()
                .map(course -> course.area().code())
                .toList();
        assertThat(firstRound).containsExactlyInAnyOrder(
                "HONGDAE", "YEONNAM", "SEONGSU"
        );
        assertThat(secondRound).containsExactlyInAnyOrder(
                "HONGDAE", "YEONNAM", "SEONGSU"
        );
    }

    @Test
    void usesCursorOffsetForNextPageRanksAndResults() {
        List<Course> candidates = new ArrayList<>();
        List<CoursePlace> coursePlaces = new ArrayList<>();
        long id = 1L;
        for (String areaCode : List.of("HONGDAE", "YEONNAM", "SEONGSU")) {
            for (int priority = 1; priority <= 2; priority++) {
                Course course = course(
                        id, areaCode, id * 10, areaCode, priority,
                        areaCode + priority
                );
                candidates.add(course);
                coursePlaces.add(representativePlace(course, "sub", "img"));
                id++;
            }
        }

        given(courseRepository.findRecommendedCourseCandidates(
                any(), any(), any()
        )).willReturn(candidates);
        given(coursePlaceRepository.findAllWithCourseAndPlaceByCourseIds(any()))
                .willReturn(coursePlaces);
        given(courseMoodTagRepository.findAllWithCourseAndMoodTagByCourseIds(any()))
                .willReturn(List.of());

        HomeRecommendedCourseListResponse firstResponse =
                service.getHomeRecommendedCourses(MEMBER_ID, null, 2);
        HomeRecommendedCourseListResponse secondResponse =
                service.getHomeRecommendedCourses(
                        MEMBER_ID,
                        firstResponse.nextCursor(),
                        2
                );

        assertThat(firstResponse.hasNext()).isTrue();
        assertThat(firstResponse.nextCursor()).isNotBlank();
        assertThat(firstResponse.courses())
                .extracting(HomeRecommendedCourseResponse::rank)
                .containsExactly(1, 2);
        assertThat(secondResponse.courses())
                .extracting(HomeRecommendedCourseResponse::rank)
                .containsExactly(3, 4);

        ArgumentCaptor<List<RecommendationResult>> resultsCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(recommendationResultRepository, times(2))
                .saveAll(resultsCaptor.capture());
        assertThat(resultsCaptor.getAllValues().get(1))
                .extracting(RecommendationResult::getRankNo)
                .containsExactly(3, 4);
    }

    @Test
    void returnsEmptyResponseAndSkipsResultSaveWhenNoCandidates() {
        given(courseRepository.findRecommendedCourseCandidates(any(), any(), any()))
                .willReturn(List.of());

        HomeRecommendedCourseListResponse response =
                service.getHomeRecommendedCourses(MEMBER_ID, null, null);

        assertThat(response.courses()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();

        // 빈 결과여도 추천 요청 기록(log)은 저장하고, result는 저장하지 않는다.
        verify(recommendationLogRepository).save(any(RecommendationLog.class));
        verify(recommendationResultRepository, never()).saveAll(any());
    }

    @Test
    void throwsInvalidCursorWhenCursorIsMalformed() {
        assertThatThrownBy(() ->
                service.getHomeRecommendedCourses(MEMBER_ID, "invalid-cursor", 3)
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(RecommendationErrorCode.INVALID_CURSOR)
                )
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(courseRepository, never())
                .findRecommendedCourseCandidates(any(), any(), any());
        verify(recommendationLogRepository, never())
                .save(any(RecommendationLog.class));
    }

    @Test
    void throwsInvalidCursorWhenCursorSignatureIsTampered() {
        String tamperedCursor = unsignedCursor("2099-01-01:2:bad-signature");

        assertThatThrownBy(() ->
                service.getHomeRecommendedCourses(MEMBER_ID, tamperedCursor, 3)
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(RecommendationErrorCode.INVALID_CURSOR)
                )
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(courseRepository, never())
                .findRecommendedCourseCandidates(any(), any(), any());
    }

    @Test
    void throwsMemberNotFoundWhenMemberDoesNotExist() {
        given(memberRepository.findById(MEMBER_ID))
                .willReturn(Optional.empty());
        given(courseRepository.findRecommendedCourseCandidates(any(), any(), any()))
                .willReturn(List.of());

        assertThatThrownBy(() ->
                service.getHomeRecommendedCourses(MEMBER_ID, null, null)
        )
                .isInstanceOfSatisfying(
                        MemberException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
                );

        verify(recommendationLogRepository, never())
                .save(any(RecommendationLog.class));
    }

    @Test
    void throwsRecommendationExceptionWhenRequestContextSerializationFails()
            throws JsonProcessingException {
        ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
        JsonProcessingException serializationFailure =
                new JsonProcessingException("boom") {
                };
        HomeRecommendedCourseService failingService =
                new HomeRecommendedCourseService(
                        courseRepository,
                        coursePlaceRepository,
                        courseMoodTagRepository,
                        memberRepository,
                        recommendationLogRepository,
                        recommendationResultRepository,
                        failingObjectMapper
                );

        given(memberRepository.findById(MEMBER_ID))
                .willReturn(Optional.of(mock(Member.class)));
        given(courseRepository.findRecommendedCourseCandidates(any(), any(), any()))
                .willReturn(List.of());
        given(failingObjectMapper.writeValueAsString(any()))
                .willThrow(serializationFailure);

        assertThatThrownBy(() ->
                failingService.getHomeRecommendedCourses(MEMBER_ID, null, null)
        )
                .isInstanceOf(RecommendationException.class)
                .hasCause(serializationFailure)
                .extracting(exception ->
                        ((RecommendationException) exception).getErrorCode()
                )
                .isEqualTo(
                        RecommendationErrorCode
                                .REQUEST_CONTEXT_SERIALIZATION_FAILED
                );

        verify(recommendationLogRepository, never())
                .save(any(RecommendationLog.class));
    }

    @Test
    void throwsInvalidSizeWhenSizeOutOfRange() {
        assertThatThrownBy(() ->
                service.getHomeRecommendedCourses(MEMBER_ID, null, 0)
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(RecommendationErrorCode.INVALID_SIZE)
                );

        assertThatThrownBy(() ->
                service.getHomeRecommendedCourses(MEMBER_ID, null, 19)
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(RecommendationErrorCode.INVALID_SIZE)
                );

        verify(recommendationLogRepository, never())
                .save(any(RecommendationLog.class));
    }

    private Course course(
            Long id,
            String areaCode,
            Long areaId,
            String areaName,
            int operatorPriority,
            String title
    ) {
        Area area = mock(Area.class);
        given(area.getId()).willReturn(areaId);
        given(area.getCode()).willReturn(areaCode);
        given(area.getName()).willReturn(areaName);

        Course course = mock(Course.class);
        given(course.getId()).willReturn(id);
        given(course.getTitle()).willReturn(title);
        given(course.getArea()).willReturn(area);
        given(course.getOperatorPriority()).willReturn(operatorPriority);
        given(course.getCreatedAt()).willReturn(LocalDateTime.of(2026, 7, 1, 0, 0));
        return course;
    }

    private CoursePlace representativePlace(
            Course course,
            String subCategory,
            String imageUrl
    ) {
        Place place = mock(Place.class);
        given(place.getSubCategory()).willReturn(subCategory);
        given(place.getDefaultImageUrl()).willReturn(imageUrl);

        CoursePlace coursePlace = mock(CoursePlace.class);
        given(coursePlace.getCourse()).willReturn(course);
        given(coursePlace.getPlace()).willReturn(place);
        given(coursePlace.getIsRepresentative()).willReturn(true);
        return coursePlace;
    }

    private CourseMoodTag moodTag(Course course, String code, String name) {
        MoodTag moodTag = mock(MoodTag.class);
        given(moodTag.getCode()).willReturn(code);
        given(moodTag.getName()).willReturn(name);

        CourseMoodTag courseMoodTag = mock(CourseMoodTag.class);
        given(courseMoodTag.getCourse()).willReturn(course);
        given(courseMoodTag.getMoodTag()).willReturn(moodTag);
        return courseMoodTag;
    }

    private String unsignedCursor(String raw) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}

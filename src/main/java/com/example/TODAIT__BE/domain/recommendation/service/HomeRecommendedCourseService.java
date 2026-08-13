package com.example.TODAIT__BE.domain.recommendation.service;

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
import com.example.TODAIT__BE.domain.recommendation.code.HomeRecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationLogErrorCode;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendationResponse;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import com.example.TODAIT__BE.domain.recommendation.enums.RecommendationType;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationResultRepository;
import com.example.TODAIT__BE.domain.recommendation.service.support.HomeRecommendationCursor;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeRecommendedCourseService {

    private static final int DEFAULT_SIZE = 3;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 18;
    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    // MVP 지원 지역 및 기본 노출 순서
    private static final List<String> AREA_CODES =
            List.of("HONGDAE", "YEONNAM", "SEONGSU");

    private final CourseRepository courseRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final CourseMoodTagRepository courseMoodTagRepository;
    private final MemberRepository memberRepository;
    private final RecommendationLogRepository recommendationLogRepository;
    private final RecommendationResultRepository recommendationResultRepository;
    private final ObjectMapper objectMapper;

    @Value("${recommendation.cursor-secret:${jwt.secret:test-home-recommendation-cursor-secret}}")
    private String cursorSecret = "test-home-recommendation-cursor-secret";

    public HomeRecommendedCourseService(
            CourseRepository courseRepository,
            CoursePlaceRepository coursePlaceRepository,
            CourseMoodTagRepository courseMoodTagRepository,
            MemberRepository memberRepository,
            RecommendationLogRepository recommendationLogRepository,
            RecommendationResultRepository recommendationResultRepository,
            ObjectMapper objectMapper
    ) {
        this.courseRepository = courseRepository;
        this.coursePlaceRepository = coursePlaceRepository;
        this.courseMoodTagRepository = courseMoodTagRepository;
        this.memberRepository = memberRepository;
        this.recommendationLogRepository = recommendationLogRepository;
        this.recommendationResultRepository = recommendationResultRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public HomeRecommendationResponse.CourseList getHomeRecommendedCourses(
            Long memberId,
            String cursorParam,
            Integer sizeParam
    ) {
        int size = resolveSize(sizeParam);

        LocalDate today = LocalDate.now(SERVICE_ZONE_ID);
        HomeRecommendationCursor cursor =
                HomeRecommendationCursor.decodeOrFirst(
                        cursorParam,
                        today,
                        cursorSecret
                );
        LocalDate rotationDate = cursor.rotationDate();
        long from = cursor.offset();
        long epochDay = rotationDate.toEpochDay();

        // 1. 추천 후보 조회 및 지역별 그룹화 → 전체 추천 순서 확정
        List<Course> fullOrder = buildFullRecommendedOrder(epochDay);

        long totalElements = fullOrder.size();
        boolean hasNext = from + size < totalElements;
        String nextCursor = hasNext
                ? new HomeRecommendationCursor(
                        rotationDate,
                        from + size
                ).encode(cursorSecret)
                : null;

        List<Course> pageCourses = slice(fullOrder, from, size);

        // 2. 추천 요청 기록 저장 (log 1건)
        RecommendationLog log = saveRecommendationLog(
                memberId, rotationDate, from, size
        );

        // 3. 응답 코스 상세 구성 + 추천 결과 저장 (result N건)
        List<HomeRecommendationResponse.CourseItem> courses =
                buildCourseResponses(pageCourses, log, from);

        return new HomeRecommendationResponse.CourseList(
                log.getId(),
                size,
                hasNext,
                nextCursor,
                courses
        );
    }

    private int resolveSize(Integer sizeParam) {
        if (sizeParam == null) {
                    return DEFAULT_SIZE;
        }
        if (sizeParam < MIN_SIZE || sizeParam > MAX_SIZE) {
            throw new RecommendationException(
                    HomeRecommendationErrorCode.INVALID_SIZE
            );
        }
        return sizeParam;
    }

    /**
     * 후보 코스를 지역별로 그룹화하고, 날짜 로테이션과 운영자 우선순위를 적용해
     * 전체 추천 노출 순서를 확정한다. (page/size 적용 전 전체 순서)
     */
    private List<Course> buildFullRecommendedOrder(long epochDay) {
        List<Course> candidates = courseRepository.findRecommendedCourseCandidates(
                CourseVisibility.RECOMMENDED,
                CourseSourceType.SERVICE_CREATED,
                AREA_CODES
        );

        if (candidates.isEmpty()) {
            return List.of();
        }

        // course_place가 1개 이상인 코스만 후보로 사용
        Map<Long, List<CoursePlace>> coursePlacesByCourseId =
                loadCoursePlaces(candidates);

        // 지역별 그룹화: 지역 내부 순서는 Repository 정렬 결과를 유지한다.
        Map<String, List<Course>> groupByArea = new LinkedHashMap<>();
        for (String areaCode : AREA_CODES) {
            groupByArea.put(areaCode, new ArrayList<>());
        }
        for (Course course : candidates) {
            List<CoursePlace> coursePlaces =
                    coursePlacesByCourseId.get(course.getId());
            if (coursePlaces == null || coursePlaces.isEmpty()) {
                continue;
            }
            List<Course> group = groupByArea.get(course.getArea().getCode());
            if (group != null) {
                group.add(course);
            }
        }

        // 지역 응답 순서 로테이션
        List<String> areaOrder = rotatedAreaOrder(epochDay);

        // 지역 균형 배치: 각 지역 1개씩 우선, 지역별 rotationIndex부터 순환
        int maxGroupSize = groupByArea.values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        List<Course> fullOrder = new ArrayList<>();
        for (int round = 0; round < maxGroupSize; round++) {
            for (String areaCode : areaOrder) {
                List<Course> group = groupByArea.get(areaCode);
                if (group == null || round >= group.size()) {
                    continue;
                }
                int rotationIndex = (int) Math.floorMod(epochDay, group.size());
                int index = (rotationIndex + round) % group.size();
                fullOrder.add(group.get(index));
            }
        }

        return fullOrder;
    }

    private List<String> rotatedAreaOrder(long epochDay) {
        int shift = (int) Math.floorMod(epochDay, AREA_CODES.size());
        List<String> rotated = new ArrayList<>(AREA_CODES.size());
        for (int i = 0; i < AREA_CODES.size(); i++) {
            rotated.add(AREA_CODES.get((i + shift) % AREA_CODES.size()));
        }
        return rotated;
    }

    private Map<Long, List<CoursePlace>> loadCoursePlaces(List<Course> candidates) {
        List<Long> courseIds = candidates.stream()
                .map(Course::getId)
                .toList();

        Map<Long, List<CoursePlace>> result = new LinkedHashMap<>();
        for (CoursePlace coursePlace :
                coursePlaceRepository.findAllWithCourseAndPlaceByCourseIds(courseIds)) {
            result.computeIfAbsent(
                    coursePlace.getCourse().getId(),
                    ignored -> new ArrayList<>()
            ).add(coursePlace);
        }
        return result;
    }

    private List<Course> slice(List<Course> fullOrder, long from, int size) {
        if (from >= fullOrder.size()) {
            return List.of();
        }
        int start = (int) from;
        int end = Math.min(start + size, fullOrder.size());
        return new ArrayList<>(fullOrder.subList(start, end));
    }

    private RecommendationLog saveRecommendationLog(
            Long memberId,
            LocalDate rotationDate,
            long offset,
            int size
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        RecommendationLog log = RecommendationLog.builder()
                .member(member)
                .recommendationType(RecommendationType.HOME_POPULAR_COURSE)
                .requestContext(buildRequestContext(rotationDate, offset, size))
                .build();

        return recommendationLogRepository.save(log);
    }

    private List<HomeRecommendationResponse.CourseItem> buildCourseResponses(
            List<Course> pageCourses,
            RecommendationLog log,
            long offset
    ) {
        if (pageCourses.isEmpty()) {
            return List.of();
        }

        List<Long> pageCourseIds = pageCourses.stream()
                .map(Course::getId)
                .toList();

        Map<Long, List<CoursePlace>> coursePlacesByCourseId =
                loadCoursePlaces(pageCourses);
        Map<Long, MoodTag> representativeMoodTagByCourseId =
                loadRepresentativeMoodTags(pageCourseIds);

        List<HomeRecommendationResponse.CourseItem> responses =
                new ArrayList<>(pageCourses.size());
        List<RecommendationResult> results =
                new ArrayList<>(pageCourses.size());

        for (int index = 0; index < pageCourses.size(); index++) {
            Course course = pageCourses.get(index);
            int rank = Math.toIntExact(offset + index + 1);
            List<CoursePlace> coursePlaces =
                    coursePlacesByCourseId.getOrDefault(
                            course.getId(), List.of()
                    );
            CoursePlace representativePlace =
                    findRepresentativePlace(coursePlaces);

            responses.add(new HomeRecommendationResponse.CourseItem(
                    course.getId(),
                    course.getTitle(),
                    toAreaResponse(course),
                    resolveRepresentativeImageUrl(representativePlace),
                    buildTags(
                            representativeMoodTagByCourseId.get(course.getId()),
                            representativePlace
                    ),
                    coursePlaces.size(),
                    rank,
                    true
            ));

            results.add(RecommendationResult.forCourse(log, course, rank, null));
        }

        recommendationResultRepository.saveAll(results);
        return responses;
    }

    private Map<Long, MoodTag> loadRepresentativeMoodTags(List<Long> courseIds) {
        Map<Long, MoodTag> result = new LinkedHashMap<>();
        for (CourseMoodTag courseMoodTag :
                courseMoodTagRepository.findAllWithCourseAndMoodTagByCourseIds(courseIds)) {
            // course_id in 조회 결과가 course_id ASC, id ASC 정렬이므로 첫 태그가 대표값
            result.putIfAbsent(
                    courseMoodTag.getCourse().getId(),
                    courseMoodTag.getMoodTag()
            );
        }
        return result;
    }

    private CoursePlace findRepresentativePlace(List<CoursePlace> coursePlaces) {
        return coursePlaces.stream()
                .filter(coursePlace ->
                        Boolean.TRUE.equals(coursePlace.getIsRepresentative()))
                .findFirst()
                .orElse(null);
    }

    private HomeRecommendationResponse.CourseArea toAreaResponse(Course course) {
        return new HomeRecommendationResponse.CourseArea(
                course.getArea().getId(),
                course.getArea().getCode(),
                course.getArea().getName()
        );
    }

    private String resolveRepresentativeImageUrl(CoursePlace representativePlace) {
        if (representativePlace == null || representativePlace.getPlace() == null) {
            return null;
        }
        return representativePlace.getPlace().getDefaultImageUrl();
    }

    private List<HomeRecommendationResponse.CourseTag> buildTags(
            MoodTag representativeMoodTag,
            CoursePlace representativePlace
    ) {
        List<HomeRecommendationResponse.CourseTag> tags = new ArrayList<>(2);

        if (representativeMoodTag != null) {
            tags.add(HomeRecommendationResponse.CourseTag.mood(
                    representativeMoodTag.getCode(),
                    representativeMoodTag.getName()
            ));
        }

        String subCategory = representativePlace != null
                && representativePlace.getPlace() != null
                ? representativePlace.getPlace().getSubCategory()
                : null;
        if (subCategory != null && !subCategory.isBlank()) {
            tags.add(HomeRecommendationResponse.CourseTag.subCategory(subCategory));
        }

        return tags;
    }

    private String buildRequestContext(LocalDate rotationDate, long offset, int size) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("sourcePolicy", "RESEARCH_DOCUMENT");
        context.put("candidatePolicy", "OPERATOR_RECOMMENDED_18_COURSES");
        context.put("includedAreaCodes", AREA_CODES);
        context.put("areaBalancePolicy", "ONE_COURSE_PER_AREA_FIRST");
        context.put("rotationPolicy", "DAILY_AREA_AND_PRIORITY_ROTATION");
        context.put("rotationDate", rotationDate.toString());
        context.put("areaOrder", rotatedAreaOrder(rotationDate.toEpochDay()));
        context.put("sortPolicy", List.of(
                "DAILY_ROTATION",
                "OPERATOR_PRIORITY_ASC",
                "CREATED_AT_ASC",
                "COURSE_ID_ASC"
        ));
        context.put("offset", offset);
        context.put("limit", size);
        context.put("policyVersion", "HOME_COURSE_V1");

        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException exception) {
            throw new RecommendationException(
                    RecommendationLogErrorCode.REQUEST_CONTEXT_SERIALIZATION_FAILED,
                    exception
            );
        }
    }
}

package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.RecommendedCourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.service.support.CourseSaveSupport;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendedCourseSaveService {

    private static final int MIN_MOOD_TAG_COUNT = 2;
    private static final int MAX_MOOD_TAG_COUNT = 6;

    private final CourseRepository courseRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final CourseMoodTagRepository courseMoodTagRepository;
    private final CourseFoodCategoryRepository courseFoodCategoryRepository;
    private final MemberRepository memberRepository;
    private final CourseSaveSupport courseSaveSupport;

    @Transactional
    public RecommendedCourseSaveResponse saveRecommendedCourse(
            Long sourceCourseId,
            Long memberId
    ) {
        Course sourceCourse = findRecommendedCourse(sourceCourseId);
        Member member = findMember(memberId);

        List<CoursePlace> sourcePlaces =
                coursePlaceRepository
                        .findAllByCourseIdOrderByVisitOrderAsc(sourceCourseId);

        validateSourceCourse(sourceCourse, sourcePlaces);

        List<CourseMoodTag> sourceMoodTags =
                courseMoodTagRepository
                        .findAllByCourseIdOrderByIdAsc(sourceCourseId);

        List<CourseFoodCategory> sourceFoodCategories =
                courseFoodCategoryRepository
                        .findAllByCourseIdOrderByIdAsc(sourceCourseId);

        validateSourceCategories(sourceMoodTags, sourceFoodCategories);

        Course savedCourse = createSavedCourse(
                sourceCourse,
                member
        );

        courseSaveSupport.copyPlaces(sourcePlaces, savedCourse);
        courseSaveSupport.copyMoodTags(sourceMoodTags, savedCourse);
        courseSaveSupport.copyFoodCategories(sourceFoodCategories, savedCourse);

        return RecommendedCourseSaveResponse.of(
                sourceCourseId,
                savedCourse,
                sourcePlaces.size()
        );
    }

    private Course findRecommendedCourse(Long courseId) {
        return courseRepository
                .findActiveRecommendedCourseById(
                        courseId,
                        CourseVisibility.RECOMMENDED,
                        CourseSourceType.SERVICE_CREATED
                )
                .orElseThrow(() ->
                        new CourseException(
                                RecommendedCourseErrorCode.RECOMMENDED_COURSE_NOT_FOUND
                        )
                );
    }

    private Member findMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new MemberException(
                                MemberErrorCode.MEMBER_NOT_FOUND
                        )
                );

        if (member.getStatus() != MemberStatus.ACTIVE
                || member.getDeletedAt() != null) {
            throw new MemberException(
                    MemberErrorCode.MEMBER_NOT_FOUND
            );
        }

        return member;
    }

    private void validateSourceCourse(
            Course sourceCourse,
            List<CoursePlace> sourcePlaces
    ) {
        if (sourceCourse.getBasePlace() == null
                || sourceCourse.getArea() == null
                || sourcePlaces.isEmpty()) {
            throwRecommendedCourseNotSavable();
        }

        long basePlaceCount = sourcePlaces.stream()
                .filter(coursePlace ->
                        coursePlace.getPlaceRole() == PlaceRole.BASE
                )
                .count();

        if (basePlaceCount != 1) {
            throwRecommendedCourseNotSavable();
        }

        long selectedPlaceCount = sourcePlaces.stream()
                .filter(coursePlace ->
                        coursePlace.getPlaceRole() == PlaceRole.SELECTED
                )
                .count();

        if (selectedPlaceCount < 1) {
            throwRecommendedCourseNotSavable();
        }

        CoursePlace baseCoursePlace = sourcePlaces.stream()
                .filter(coursePlace ->
                        coursePlace.getPlaceRole() == PlaceRole.BASE
                )
                .findFirst()
                .orElseThrow(this::recommendedCourseNotSavable);

        if (sourceCourse.getBasePlace().getId() == null
                || baseCoursePlace.getPlace() == null
                || baseCoursePlace.getPlace().getId() == null
                || !sourceCourse.getBasePlace().getId()
                .equals(baseCoursePlace.getPlace().getId())) {
            throwRecommendedCourseNotSavable();
        }

        Set<Integer> visitOrders = new HashSet<>();
        Set<Long> placeIds = new HashSet<>();

        for (int index = 0; index < sourcePlaces.size(); index++) {
            CoursePlace coursePlace = sourcePlaces.get(index);
            Integer visitOrder = coursePlace.getVisitOrder();

            if (coursePlace.getPlaceRole() == null) {
                throwRecommendedCourseNotSavable();
            }

            if (visitOrder == null
                    || !visitOrders.add(visitOrder)
                    || visitOrder != index + 1) {
                throwRecommendedCourseNotSavable();
            }

            if (coursePlace.getPlaceRole() == PlaceRole.BASE
                    && visitOrder != 1) {
                throwRecommendedCourseNotSavable();
            }

            if (coursePlace.getPlaceRole() == PlaceRole.SELECTED
                    && visitOrder < 2) {
                throwRecommendedCourseNotSavable();
            }

            if (coursePlace.getPlace() == null
                    || coursePlace.getPlace().getId() == null
                    || !placeIds.add(coursePlace.getPlace().getId())) {
                throwRecommendedCourseNotSavable();
            }
        }
    }

    private void validateSourceCategories(
            List<CourseMoodTag> sourceMoodTags,
            List<CourseFoodCategory> sourceFoodCategories
    ) {
        if (sourceMoodTags.size() < MIN_MOOD_TAG_COUNT
                || sourceMoodTags.size() > MAX_MOOD_TAG_COUNT
                || sourceFoodCategories.isEmpty()) {
            throwRecommendedCourseNotSavable();
        }

        Set<Long> moodTagIds = new HashSet<>();
        for (CourseMoodTag sourceMoodTag : sourceMoodTags) {
            Long moodTagId = sourceMoodTag.getMoodTag() == null
                    ? null
                    : sourceMoodTag.getMoodTag().getId();

            if (moodTagId == null || !moodTagIds.add(moodTagId)) {
                throwRecommendedCourseNotSavable();
            }
        }

        Set<Long> foodCategoryIds = new HashSet<>();
        for (CourseFoodCategory sourceFoodCategory : sourceFoodCategories) {
            Long foodCategoryId = sourceFoodCategory.getFoodCategory() == null
                    ? null
                    : sourceFoodCategory.getFoodCategory().getId();

            if (foodCategoryId == null
                    || !foodCategoryIds.add(foodCategoryId)) {
                throwRecommendedCourseNotSavable();
            }
        }
    }

    private CourseException recommendedCourseNotSavable() {
        return new CourseException(
                RecommendedCourseErrorCode.RECOMMENDED_COURSE_NOT_SAVABLE
        );
    }

    private void throwRecommendedCourseNotSavable() {
        throw recommendedCourseNotSavable();
    }

    private Course createSavedCourse(
            Course sourceCourse,
            Member member
    ) {
        Course savedCourse = Course.builder()
                .member(member)
                .basePlace(sourceCourse.getBasePlace())
                .area(sourceCourse.getArea())
                .title(sourceCourse.getTitle())
                .memo(sourceCourse.getMemo())
                .visibility(CourseVisibility.PRIVATE)
                .sourceType(CourseSourceType.USER_CREATED)
                .build();

        return courseRepository.save(savedCourse);
    }

}

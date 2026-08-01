package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendedCourseSaveService {

    private final CourseRepository courseRepository;
    private final CoursePlaceRepository coursePlaceRepository;
    private final CourseMoodTagRepository courseMoodTagRepository;
    private final CourseFoodCategoryRepository courseFoodCategoryRepository;
    private final MemberRepository memberRepository;

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

        Course savedCourse = createSavedCourse(
                sourceCourse,
                member,
                sourcePlaces.size()
        );

        copyCoursePlaces(sourcePlaces, savedCourse);
        copyCourseMoodTags(sourceMoodTags, savedCourse);
        copyCourseFoodCategories(sourceFoodCategories, savedCourse);

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
                                CourseErrorCode.RECOMMENDED_COURSE_NOT_FOUND
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
            throw new CourseException(
                    CourseErrorCode.INVALID_RECOMMENDED_COURSE
            );
        }

        long basePlaceCount = sourcePlaces.stream()
                .filter(coursePlace ->
                        coursePlace.getPlaceRole() == PlaceRole.BASE
                )
                .count();

        if (basePlaceCount != 1) {
            throw new CourseException(
                    CourseErrorCode.INVALID_RECOMMENDED_COURSE
            );
        }

        CoursePlace baseCoursePlace = sourcePlaces.stream()
                .filter(coursePlace ->
                        coursePlace.getPlaceRole() == PlaceRole.BASE
                )
                .findFirst()
                .orElseThrow(() ->
                        new CourseException(
                                CourseErrorCode.INVALID_RECOMMENDED_COURSE
                        )
                );

        if (!sourceCourse.getBasePlace().getId()
                .equals(baseCoursePlace.getPlace().getId())) {
            throw new CourseException(
                    CourseErrorCode.INVALID_RECOMMENDED_COURSE
            );
        }

        Set<Integer> visitOrders = new HashSet<>();

        for (int index = 0; index < sourcePlaces.size(); index++) {
            CoursePlace coursePlace = sourcePlaces.get(index);
            Integer visitOrder = coursePlace.getVisitOrder();

            if (visitOrder == null
                    || !visitOrders.add(visitOrder)
                    || visitOrder != index + 1) {
                throw new CourseException(
                        CourseErrorCode.INVALID_RECOMMENDED_COURSE
                );
            }
        }
    }

    private Course createSavedCourse(
            Course sourceCourse,
            Member member,
            int placeCount
    ) {
        Course savedCourse = Course.builder()
                .member(member)
                .basePlace(sourceCourse.getBasePlace())
                .area(sourceCourse.getArea())
                .title(sourceCourse.getTitle())
                .memo(sourceCourse.getMemo())
                .visibility(CourseVisibility.PRIVATE)
                .sourceType(CourseSourceType.USER_CREATED)
                .placeCount(placeCount)
                .build();

        return courseRepository.save(savedCourse);
    }

    private void copyCoursePlaces(
            List<CoursePlace> sourcePlaces,
            Course savedCourse
    ) {
        List<CoursePlace> copiedPlaces = sourcePlaces.stream()
                .map(sourcePlace ->
                        CoursePlace.builder()
                                .course(savedCourse)
                                .place(sourcePlace.getPlace())
                                .visitOrder(sourcePlace.getVisitOrder())
                                .placeRole(sourcePlace.getPlaceRole())
                                .isRepresentative(
                                        sourcePlace.getIsRepresentative()
                                )
                                .placeNameSnapshot(
                                        sourcePlace.getPlaceNameSnapshot()
                                )
                                .addressSnapshot(
                                        sourcePlace.getAddressSnapshot()
                                )
                                .latitudeSnapshot(
                                        sourcePlace.getLatitudeSnapshot()
                                )
                                .longitudeSnapshot(
                                        sourcePlace.getLongitudeSnapshot()
                                )
                                .categorySnapshot(
                                        sourcePlace.getCategorySnapshot()
                                )
                                .memo(sourcePlace.getMemo())
                                .build()
                )
                .toList();

        coursePlaceRepository.saveAll(copiedPlaces);
    }

    private void copyCourseMoodTags(
            List<CourseMoodTag> sourceMoodTags,
            Course savedCourse
    ) {
        List<CourseMoodTag> copiedMoodTags = sourceMoodTags.stream()
                .map(sourceMoodTag ->
                        CourseMoodTag.builder()
                                .course(savedCourse)
                                .moodTag(sourceMoodTag.getMoodTag())
                                .build()
                )
                .toList();

        courseMoodTagRepository.saveAll(copiedMoodTags);
    }

    private void copyCourseFoodCategories(
            List<CourseFoodCategory> sourceFoodCategories,
            Course savedCourse
    ) {
        List<CourseFoodCategory> copiedFoodCategories =
                sourceFoodCategories.stream()
                        .map(sourceFoodCategory ->
                                CourseFoodCategory.builder()
                                        .course(savedCourse)
                                        .foodCategory(
                                                sourceFoodCategory
                                                        .getFoodCategory()
                                        )
                                        .build()
                        )
                        .toList();

        courseFoodCategoryRepository.saveAll(copiedFoodCategories);
    }
}

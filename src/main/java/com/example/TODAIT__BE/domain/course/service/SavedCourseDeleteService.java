package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseDeleteResponse;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedCourseDeleteService {

    private final CourseRepository courseRepository;

    @Transactional
    public SavedCourseDeleteResponse deleteSavedCourse(
            Long courseId,
            Long memberId
    ) {
        Course course = courseRepository
                .findSavedCourseDetailById(courseId)
                .orElseThrow(() ->
                        new CourseException(
                                CourseErrorCode.SAVED_COURSE_NOT_FOUND
                        )
                );

        if (!course.getMember().getId().equals(memberId)) {
            throw new CourseException(
                    CourseErrorCode.SAVED_COURSE_ACCESS_DENIED
            );
        }

        if (course.getSourceType() != CourseSourceType.USER_CREATED) {
            throw new CourseException(
                    CourseErrorCode.SAVED_COURSE_ACCESS_DENIED
            );
        }

        course.softDelete(LocalDateTime.now());

        return SavedCourseDeleteResponse.of(course.getId());
    }
}

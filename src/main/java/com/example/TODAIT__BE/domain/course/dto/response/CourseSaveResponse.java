package com.example.TODAIT__BE.domain.course.dto.response;

import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import java.time.LocalDateTime;
import java.util.List;

public final class CourseSaveResponse {

    private CourseSaveResponse() {
    }

    public record SaveResponse(
            Long courseId,
            CourseDraftStatus draftStatus,
            String title,
            String memo,
            LocalDateTime savedAt,
            Integer placeCount,
            List<MoodTagItem> moodTags,
            List<FoodCategoryItem> foodCategories,
            List<CoursePlaceItem> places
    ) {

        public static SaveResponse of(
                Course course,
                List<MoodTagItem> moodTags,
                List<FoodCategoryItem> foodCategories,
                List<CoursePlaceItem> places
        ) {
            return new SaveResponse(
                    course.getId(),
                    CourseDraftStatus.COMPLETED,
                    course.getTitle(),
                    course.getMemo(),
                    course.getCreatedAt(),
                    places.size(),
                    moodTags,
                    foodCategories,
                    places
            );
        }
    }

    public record MoodTagItem(
            Long moodTagId,
            String code,
            String name
    ) {

        public static MoodTagItem from(com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag moodTag) {
            return new MoodTagItem(moodTag.getId(), moodTag.getCode(), moodTag.getName());
        }
    }

    public record FoodCategoryItem(
            Long foodCategoryId,
            String code,
            String name
    ) {

        public static FoodCategoryItem from(com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory foodCategory) {
            return new FoodCategoryItem(foodCategory.getId(), foodCategory.getCode(), foodCategory.getName());
        }
    }

    public record CoursePlaceItem(
            Long coursePlaceId,
            Long placeId,
            PlaceRole placeRole,
            Integer visitOrder,
            String name,
            String address,
            String memo
    ) {

        public static CoursePlaceItem from(CoursePlace coursePlace) {
            return new CoursePlaceItem(
                    coursePlace.getId(),
                    coursePlace.getPlace().getId(),
                    coursePlace.getPlaceRole(),
                    coursePlace.getVisitOrder(),
                    coursePlace.getPlaceNameSnapshot(),
                    coursePlace.getAddressSnapshot(),
                    coursePlace.getMemo()
            );
        }
    }
}

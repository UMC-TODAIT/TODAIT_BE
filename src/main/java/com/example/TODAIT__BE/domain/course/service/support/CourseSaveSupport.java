package com.example.TODAIT__BE.domain.course.service.support;

import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse.FoodCategoryItem;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse.MoodTagItem;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse.CoursePlaceItem;
import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.repository.CourseFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseSaveSupport {

    private final CourseMoodTagRepository courseMoodTagRepository;
    private final CourseFoodCategoryRepository courseFoodCategoryRepository;
    private final CoursePlaceRepository coursePlaceRepository;

    public List<MoodTagItem> saveMoodTags(Course course, List<MoodTag> moodTags) {
        List<MoodTagItem> responses = new ArrayList<>();
        for (MoodTag moodTag : moodTags) {
            courseMoodTagRepository.save(CourseMoodTag.builder()
                    .course(course)
                    .moodTag(moodTag)
                    .build());
            responses.add(MoodTagItem.from(moodTag));
        }
        return responses;
    }

    public List<FoodCategoryItem> saveFoodCategories(
            Course course,
            List<CourseDraftFoodCategory> draftFoodCategories
    ) {
        List<FoodCategoryItem> responses = new ArrayList<>();
        for (CourseDraftFoodCategory draftFoodCategory : draftFoodCategories) {
            FoodCategory foodCategory = draftFoodCategory.getFoodCategory();
            courseFoodCategoryRepository.save(CourseFoodCategory.builder()
                    .course(course)
                    .foodCategory(foodCategory)
                    .build());
            responses.add(FoodCategoryItem.from(foodCategory));
        }
        return responses;
    }

    public List<CoursePlaceItem> savePlaces(Course course, List<CourseDraftPlace> draftPlaces) {
        List<CoursePlaceItem> responses = new ArrayList<>();
        for (CourseDraftPlace draftPlace : draftPlaces) {
            Place place = draftPlace.getPlace();
            CoursePlace coursePlace = coursePlaceRepository.save(CoursePlace.builder()
                    .course(course)
                    .place(place)
                    .visitOrder(draftPlace.getVisitOrder())
                    .placeRole(draftPlace.getPlaceRole())
                    .placeNameSnapshot(place.getName())
                    .addressSnapshot(place.getAddress())
                    .latitudeSnapshot(place.getLatitude())
                    .longitudeSnapshot(place.getLongitude())
                    .categorySnapshot(place.getPlaceCategory().getCode())
                    .memo(draftPlace.getMemo())
                    .build());
            responses.add(CoursePlaceItem.from(coursePlace));
        }
        return responses;
    }

    public void copyPlaces(List<CoursePlace> sourcePlaces, Course savedCourse) {
        List<CoursePlace> copiedPlaces = sourcePlaces.stream()
                .map(sourcePlace -> CoursePlace.builder()
                        .course(savedCourse)
                        .place(sourcePlace.getPlace())
                        .visitOrder(sourcePlace.getVisitOrder())
                        .placeRole(sourcePlace.getPlaceRole())
                        .isRepresentative(sourcePlace.getIsRepresentative())
                        .placeNameSnapshot(sourcePlace.getPlaceNameSnapshot())
                        .addressSnapshot(sourcePlace.getAddressSnapshot())
                        .latitudeSnapshot(sourcePlace.getLatitudeSnapshot())
                        .longitudeSnapshot(sourcePlace.getLongitudeSnapshot())
                        .categorySnapshot(sourcePlace.getCategorySnapshot())
                        .memo(sourcePlace.getMemo())
                        .build())
                .toList();

        coursePlaceRepository.saveAll(copiedPlaces);
    }

    public void copyMoodTags(List<CourseMoodTag> sourceMoodTags, Course savedCourse) {
        List<CourseMoodTag> copiedMoodTags = sourceMoodTags.stream()
                .map(sourceMoodTag -> CourseMoodTag.builder()
                        .course(savedCourse)
                        .moodTag(sourceMoodTag.getMoodTag())
                        .build())
                .toList();

        courseMoodTagRepository.saveAll(copiedMoodTags);
    }

    public void copyFoodCategories(List<CourseFoodCategory> sourceFoodCategories, Course savedCourse) {
        List<CourseFoodCategory> copiedFoodCategories = sourceFoodCategories.stream()
                .map(sourceFoodCategory -> CourseFoodCategory.builder()
                        .course(savedCourse)
                        .foodCategory(sourceFoodCategory.getFoodCategory())
                        .build())
                .toList();

        courseFoodCategoryRepository.saveAll(copiedFoodCategories);
    }
}

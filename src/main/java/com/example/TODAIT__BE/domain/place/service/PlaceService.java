package com.example.TODAIT__BE.domain.place.service;

import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailCategoryResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailFoodCategoryResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailMoodTagResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceMenuResponse;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceImage;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceImageType;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.repository.PlaceFoodCategoryRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceImageRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceMenuRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceMoodTagRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.global.util.BusinessHoursCalculator;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final DateTimeFormatter LAST_ORDER_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final PlaceRepository placeRepository;
    private final PlaceMoodTagRepository placeMoodTagRepository;
    private final PlaceFoodCategoryRepository placeFoodCategoryRepository;
    private final PlaceImageRepository placeImageRepository;
    private final PlaceMenuRepository placeMenuRepository;


    public PlaceDetailResponse getPlaceDetail(Long placeId) {
        Place place = placeRepository.findDetailById(placeId)
                .orElseThrow(() ->
                        new PlaceException(PlaceErrorCode.PLACE_NOT_FOUND));

        if (!isExposable(place)) {
            throw new PlaceException(PlaceErrorCode.PLACE_NOT_EXPOSED);
        }

        return new PlaceDetailResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getRoadAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getPhone(),
                place.getSubCategory(),
                place.getDefaultImageUrl(),
                BusinessHoursCalculator.calculate(
                        place.getBusinessHours(), LocalTime.now()),
                formatLastOrderTime(place.getLastOrderTime()),
                toPlaceCategory(place.getPlaceCategory()),
                toFoodCategory(place.getPrimaryFoodCategory()),
                loadFoodCategories(placeId),
                loadMoodTags(placeId),
                loadImageUrls(placeId, PlaceImageType.MAIN),
                loadImageUrls(placeId, PlaceImageType.INTERIOR),
                loadMenus(placeId),
                place.getDefaultRecommendReason()
        );
    }

    private boolean isExposable(Place place) {
        return place.getExposureStatus() == PlaceExposureStatus.ACTIVE
                && Boolean.TRUE.equals(place.getIsActive());
    }

    private String formatLastOrderTime(LocalTime lastOrderTime) {
        return lastOrderTime != null
                ? lastOrderTime.format(LAST_ORDER_FORMAT)
                : null;
    }

    private PlaceDetailCategoryResponse toPlaceCategory(PlaceCategory placeCategory) {
        if (placeCategory == null) {
            return null;
        }
        return new PlaceDetailCategoryResponse(
                placeCategory.getId(),
                placeCategory.getCode(),
                placeCategory.getName()
        );
    }

    private PlaceDetailFoodCategoryResponse toFoodCategory(FoodCategory foodCategory) {
        if (foodCategory == null) {
            return null;
        }
        return new PlaceDetailFoodCategoryResponse(
                foodCategory.getId(),
                foodCategory.getCode(),
                foodCategory.getName()
        );
    }

    private List<PlaceDetailFoodCategoryResponse> loadFoodCategories(Long placeId) {
        return placeFoodCategoryRepository
                .findAllByPlaceIdOrderByIdAsc(placeId)
                .stream()
                .map(placeFoodCategory ->
                        toFoodCategory(placeFoodCategory.getFoodCategory()))
                .toList();
    }

    private List<PlaceDetailMoodTagResponse> loadMoodTags(Long placeId) {
        return placeMoodTagRepository
                .findAllByPlaceIdOrderByIdAsc(placeId)
                .stream()
                .map(placeMoodTag -> {
                    MoodTag moodTag = placeMoodTag.getMoodTag();
                    return new PlaceDetailMoodTagResponse(
                            moodTag.getId(),
                            moodTag.getCode(),
                            moodTag.getName()
                    );
                })
                .toList();
    }

    private List<String> loadImageUrls(Long placeId, PlaceImageType imageType) {
        return placeImageRepository
                .findAllByPlaceIdAndImageTypeOrderByDisplayOrderAscIdAsc(
                        placeId, imageType)
                .stream()
                .map(PlaceImage::getImageUrl)
                .toList();
    }

    private List<PlaceMenuResponse> loadMenus(Long placeId) {
        return placeMenuRepository
                .findAllByPlaceIdOrderByDisplayOrderAscIdAsc(placeId)
                .stream()
                .map(menu -> new PlaceMenuResponse(
                        menu.getId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getImageUrl()
                ))
                .toList();
    }


}

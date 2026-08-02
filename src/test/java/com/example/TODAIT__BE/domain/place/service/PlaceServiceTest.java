package com.example.TODAIT__BE.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailResponse;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceFoodCategory;
import com.example.TODAIT__BE.domain.place.entity.PlaceImage;
import com.example.TODAIT__BE.domain.place.entity.PlaceMenu;
import com.example.TODAIT__BE.domain.place.entity.PlaceMoodTag;
import com.example.TODAIT__BE.domain.place.enums.BusinessStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceImageType;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.repository.PlaceFoodCategoryRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceImageRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceMenuRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceMoodTagRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private PlaceMoodTagRepository placeMoodTagRepository;
    @Mock
    private PlaceFoodCategoryRepository placeFoodCategoryRepository;
    @Mock
    private PlaceImageRepository placeImageRepository;
    @Mock
    private PlaceMenuRepository placeMenuRepository;

    @InjectMocks
    private PlaceService placeService;


    @Test
    void returnsPlaceDetailWithAllMappedSections() {
        // 중첩 given() 방지: mock 컬렉션을 먼저 구성한 뒤 스텁에 전달
        Place place = exposablePlace(1L);
        List<PlaceFoodCategory> foodCategories =
                List.of(placeFoodCategory(6L, "DESSERT", "디저트"));
        List<PlaceMoodTag> moodTags = List.of(
                placeMoodTag(1L, "HIP", "힙한"),
                placeMoodTag(4L, "ROMANTIC", "로맨틱")
        );
        List<PlaceImage> mainImages = List.of(
                placeImage("https://img/main1.jpg"),
                placeImage("https://img/main2.jpg")
        );
        List<PlaceImage> interiorImages = List.of(placeImage("https://img/interior1.jpg"));
        List<PlaceMenu> menus = List.of(
                placeMenu(1L, "과일 소르베", 13000, "https://img/menu1.jpg"),
                placeMenu(2L, "생과일 파르페", null, "https://img/menu2.jpg")
        );

        given(placeRepository.findDetailById(1L)).willReturn(Optional.of(place));
        given(placeFoodCategoryRepository.findAllByPlaceIdOrderByIdAsc(1L))
                .willReturn(foodCategories);
        given(placeMoodTagRepository.findAllByPlaceIdOrderByIdAsc(1L))
                .willReturn(moodTags);
        given(placeImageRepository
                .findAllByPlaceIdAndImageTypeOrderByDisplayOrderAscIdAsc(1L, PlaceImageType.MAIN))
                .willReturn(mainImages);
        given(placeImageRepository
                .findAllByPlaceIdAndImageTypeOrderByDisplayOrderAscIdAsc(1L, PlaceImageType.INTERIOR))
                .willReturn(interiorImages);
        given(placeMenuRepository.findAllByPlaceIdOrderByDisplayOrderAscIdAsc(1L))
                .willReturn(menus);

        PlaceDetailResponse response = placeService.getPlaceDetail(1L);

        assertThat(response.placeId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("투데잇 카페");
        assertThat(response.subCategory()).isEqualTo("디저트 카페");
        assertThat(response.businessStatus()).isEqualTo(BusinessStatus.OPEN);
        assertThat(response.lastOrderTime()).isEqualTo("20:30");
        assertThat(response.placeCategory().code()).isEqualTo("CAFE");
        assertThat(response.primaryFoodCategory().code()).isEqualTo("DESSERT");
        assertThat(response.foodCategories()).extracting("code").containsExactly("DESSERT");
        assertThat(response.moodTags()).extracting("code").containsExactly("HIP", "ROMANTIC");
        assertThat(response.imageUrls())
                .containsExactly("https://img/main1.jpg", "https://img/main2.jpg");
        assertThat(response.interiorImageUrls())
                .containsExactly("https://img/interior1.jpg");
        assertThat(response.menus()).hasSize(2);
        assertThat(response.menus().get(1).price()).isNull(); // 가격 변동 메뉴
    }

    @Test
    void throwsNotFoundWhenPlaceMissingOrDeleted() {
        given(placeRepository.findDetailById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> placeService.getPlaceDetail(99L))
                .isInstanceOfSatisfying(PlaceException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(PlaceErrorCode.PLACE_NOT_FOUND));

        verify(placeMenuRepository, never())
                .findAllByPlaceIdOrderByDisplayOrderAscIdAsc(any());
    }

    @Test
    void throwsNotExposedWhenPlaceIsNotActive() {
        Place place = mock(Place.class);
        given(place.getExposureStatus()).willReturn(PlaceExposureStatus.INACTIVE);
        given(place.getIsActive()).willReturn(true);
        given(placeRepository.findDetailById(2L)).willReturn(Optional.of(place));

        assertThatThrownBy(() -> placeService.getPlaceDetail(2L))
                .isInstanceOfSatisfying(PlaceException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(PlaceErrorCode.PLACE_NOT_EXPOSED));

        verify(placeMoodTagRepository, never()).findAllByPlaceIdOrderByIdAsc(any());
    }

    private Place exposablePlace(Long id) {
        Place place = mock(Place.class);
        given(place.getId()).willReturn(id);
        given(place.getName()).willReturn("투데잇 카페");
        given(place.getAddress()).willReturn("서울 마포구 와우산로 00");
        given(place.getRoadAddress()).willReturn("서울 마포구 와우산로 00길 00");
        given(place.getLatitude()).willReturn(37.5563);
        given(place.getLongitude()).willReturn(126.9236);
        given(place.getPhone()).willReturn("02-1234-5678");
        given(place.getSubCategory()).willReturn("디저트 카페");
        given(place.getDefaultImageUrl()).willReturn("https://img/main1.jpg");
        given(place.getBusinessHours()).willReturn("00:00-00:00"); // 24시간 → OPEN
        given(place.getLastOrderTime()).willReturn(LocalTime.of(20, 30));
        given(place.getDefaultRecommendReason()).willReturn("감성적인 분위기");
        given(place.getExposureStatus()).willReturn(PlaceExposureStatus.ACTIVE);
        given(place.getIsActive()).willReturn(true);
        // 중첩 given() 방지: 연관 mock을 먼저 만든 뒤 스텁에 전달
        PlaceCategory placeCategory = placeCategory(1L, "CAFE", "카페");
        FoodCategory primaryFoodCategory = foodCategory(6L, "DESSERT", "디저트");
        given(place.getPlaceCategory()).willReturn(placeCategory);
        given(place.getPrimaryFoodCategory()).willReturn(primaryFoodCategory);
        return place;
    }

    private PlaceCategory placeCategory(Long id, String code, String name) {
        PlaceCategory category = mock(PlaceCategory.class);
        given(category.getId()).willReturn(id);
        given(category.getCode()).willReturn(code);
        given(category.getName()).willReturn(name);
        return category;
    }

    private FoodCategory foodCategory(Long id, String code, String name) {
        FoodCategory category = mock(FoodCategory.class);
        given(category.getId()).willReturn(id);
        given(category.getCode()).willReturn(code);
        given(category.getName()).willReturn(name);
        return category;
    }

    private PlaceFoodCategory placeFoodCategory(Long id, String code, String name) {
        FoodCategory foodCategory = foodCategory(id, code, name);
        PlaceFoodCategory placeFoodCategory = mock(PlaceFoodCategory.class);
        given(placeFoodCategory.getFoodCategory()).willReturn(foodCategory);
        return placeFoodCategory;
    }

    private PlaceMoodTag placeMoodTag(Long id, String code, String name) {
        MoodTag moodTag = mock(MoodTag.class);
        given(moodTag.getId()).willReturn(id);
        given(moodTag.getCode()).willReturn(code);
        given(moodTag.getName()).willReturn(name);
        PlaceMoodTag placeMoodTag = mock(PlaceMoodTag.class);
        given(placeMoodTag.getMoodTag()).willReturn(moodTag);
        return placeMoodTag;
    }

    private PlaceImage placeImage(String url) {
        PlaceImage image = mock(PlaceImage.class);
        given(image.getImageUrl()).willReturn(url);
        return image;
    }

    private PlaceMenu placeMenu(Long id, String name, Integer price, String imageUrl) {
        PlaceMenu menu = mock(PlaceMenu.class);
        given(menu.getId()).willReturn(id);
        given(menu.getName()).willReturn(name);
        given(menu.getPrice()).willReturn(price);
        given(menu.getImageUrl()).willReturn(imageUrl);
        return menu;
    }
}

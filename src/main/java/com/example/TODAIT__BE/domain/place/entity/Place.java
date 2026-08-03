package com.example.TODAIT__BE.domain.place.entity;

import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "투데잇 장소 PK")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false, comment = "지원 지역 FK")
    private Area area;

    @Column(nullable = false, comment = "장소명")
    private String name;

    @Column(nullable = false, comment = "지번 주소")
    private String address;

    @Column(name = "road_address", comment = "도로명 주소")
    private String roadAddress;

    @Column(nullable = false, comment = "위도")
    private Double latitude;

    @Column(nullable = false, comment = "경도")
    private Double longitude;

    @Column(comment = "전화번호")
    private String phone;

    @Column(name = "sub_category", comment = "세부 카테고리")
    private String subCategory;

    @Column(name = "default_image_url", comment = "대표 이미지 URL")
    private String defaultImageUrl;

    @Column(name = "default_recommend_reason", comment = "기본 추천 이유")
    private String defaultRecommendReason;

    @Column(name = "business_hours", comment = "영업시간 원본 데이터 (예: HH:mm-HH:mm)")
    private String businessHours;

    @Column(name = "last_order_time", comment = "라스트오더 시각")
    private java.time.LocalTime lastOrderTime;

    @Builder.Default
    @Column(name = "operator_priority", nullable = false, comment = "운영자 우선순위")
    private Integer operatorPriority = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_category_id", nullable = false, comment = "장소 대분류 FK")
    private PlaceCategory placeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_food_category_id", comment = "대표 음식 카테고리 FK")
    private FoodCategory primaryFoodCategory;

    @Builder.Default
    @OneToMany(mappedBy = "place")
    private List<PlaceMoodTag> placeMoodTags = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "place")
    private List<PlaceFoodCategory> placeFoodCategories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "place")
    private List<PlaceImage> placeImages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "exposure_status", nullable = false, comment = "사용자 앱 노출 상태")
    private PlaceExposureStatus exposureStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, comment = "운영자 검수 상태")
    private PlaceReviewStatus reviewStatus;

    @Builder.Default
    @Column(name = "is_active", nullable = false, comment = "앱 추천/검색 사용 여부")
    private Boolean isActive = false;

    @Builder.Default
    @Column(name = "is_reviewed", nullable = false, comment = "운영자 검수 완료 여부")
    private Boolean isReviewed = false;

    @Column(name = "admin_memo", comment = "운영자 메모")
    private String adminMemo;

    @Builder.Default
    @Column(name = "selected_count", nullable = false, comment = "코스 추가 횟수")
    private Long selectedCount = 0L;

    @Builder.Default
    @Column(name = "saved_count", nullable = false, comment = "저장된 코스 포함 횟수")
    private Long savedCount = 0L;

    @Builder.Default
    @Column(name = "viewed_count", nullable = false, comment = "조회 횟수")
    private Long viewedCount = 0L;

    @Builder.Default
    @Column(name = "popularity_score", nullable = false, precision = 10, scale = 4, comment = "내부 인기도 점수")
    private BigDecimal popularityScore = BigDecimal.ZERO;

    @Column(name = "deleted_at", comment = "소프트 삭제 시각")
    private LocalDateTime deletedAt;

    public static Place createFromExternalSource(
            Area area,
            PlaceCategory placeCategory,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude,
            String phone,
            String subCategory
    ) {
        return Place.builder()
                .area(area)
                .placeCategory(placeCategory)
                .name(name)
                .address(address)
                .roadAddress(roadAddress)
                .latitude(latitude)
                .longitude(longitude)
                .phone(phone)
                .subCategory(subCategory)
                .exposureStatus(PlaceExposureStatus.ACTIVE)
                .reviewStatus(PlaceReviewStatus.BEFORE_REVIEW)
                .isActive(true)
                .isReviewed(false)
                .build();
    }
}

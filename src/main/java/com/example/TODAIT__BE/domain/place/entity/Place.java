package com.example.TODAIT__BE.domain.place.entity;

import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(name = "road_address")
    private String roadAddress;

    private Double latitude;

    private Double longitude;

    @Column(name = "default_image_url")
    private String defaultImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_category_id")
    private PlaceCategory placeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_food_category_id")
    private FoodCategory primaryFoodCategory;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "place_mood_tag",
            joinColumns = @JoinColumn(name = "place_id"),
            inverseJoinColumns = @JoinColumn(name = "mood_tag_id")
    )
    private List<MoodTag> moodTags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "exposure_status", nullable = false)
    private PlaceExposureStatus exposureStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private PlaceReviewStatus reviewStatus;
}

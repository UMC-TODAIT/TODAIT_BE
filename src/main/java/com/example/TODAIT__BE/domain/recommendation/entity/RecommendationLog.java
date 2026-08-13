package com.example.TODAIT__BE.domain.recommendation.entity;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.recommendation.enums.RecommendationType;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommendation_log")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type", nullable = false)
    private RecommendationType recommendationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_draft_id")
    private CourseDraft courseDraft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_place_id")
    private Place basePlace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_category_id")
    private PlaceCategory placeCategory;

    @Column(name = "user_latitude")
    private Double userLatitude;

    @Column(name = "user_longitude")
    private Double userLongitude;

    @Column(name = "request_context", columnDefinition = "TEXT")
    private String requestContext;
}

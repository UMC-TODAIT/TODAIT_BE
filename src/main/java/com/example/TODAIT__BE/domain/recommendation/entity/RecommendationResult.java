package com.example.TODAIT__BE.domain.recommendation.entity;

import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommendation_result")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_log_id", nullable = false)
    private RecommendationLog recommendationLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "rank_no", nullable = false)
    private Integer rankNo;

    @Column(name = "reason_text")
    private String reasonText;

    @Column(name = "distance_meters")
    private Integer distanceMeters;

    @Column(name = "matched_mood_count")
    private Integer matchedMoodCount;

    @Column(name = "matched_food_count")
    private Integer matchedFoodCount;

    @Column(name = "internal_score", precision = 10, scale = 4)
    private BigDecimal internalScore;

    /**
     * 코스 추천 결과를 생성한다. course_id는 반드시 존재하고 place_id는 null이어야 한다.
     */
    public static RecommendationResult forCourse(
            RecommendationLog recommendationLog,
            Course course,
            int rankNo,
            String reasonText
    ) {
        if (recommendationLog == null) {
            throw new IllegalArgumentException("recommendationLog는 null일 수 없습니다.");
        }
        if (course == null) {
            throw new IllegalArgumentException("코스 추천 결과의 course는 null일 수 없습니다.");
        }
        return RecommendationResult.builder()
                .recommendationLog(recommendationLog)
                .course(course)
                .place(null)
                .rankNo(rankNo)
                .reasonText(reasonText)
                .build();
    }

    /**
     * 장소 추천 결과를 생성한다. place_id는 반드시 존재하고 course_id는 null이어야 한다.
     */
    public static RecommendationResult forPlace(
            RecommendationLog recommendationLog,
            Place place,
            int rankNo,
            String reasonText,
            Integer distanceMeters,
            Integer matchedMoodCount,
            Integer matchedFoodCount,
            BigDecimal internalScore
    ) {
        if (recommendationLog == null) {
            throw new IllegalArgumentException("recommendationLog는 null일 수 없습니다.");
        }
        if (place == null) {
            throw new IllegalArgumentException("장소 추천 결과의 place는 null일 수 없습니다.");
        }
        return RecommendationResult.builder()
                .recommendationLog(recommendationLog)
                .place(place)
                .course(null)
                .rankNo(rankNo)
                .reasonText(reasonText)
                .distanceMeters(distanceMeters)
                .matchedMoodCount(matchedMoodCount)
                .matchedFoodCount(matchedFoodCount)
                .internalScore(internalScore)
                .build();
    }
}

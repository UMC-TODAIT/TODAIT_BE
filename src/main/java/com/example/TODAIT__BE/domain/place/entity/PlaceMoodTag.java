package com.example.TODAIT__BE.domain.place.entity;

import com.example.TODAIT__BE.domain.place.enums.PlaceTagSource;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "place_mood_tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_place_mood_tag_place_mood",
                columnNames = {"place_id", "mood_tag_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceMoodTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "장소 분위기 태그 PK")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false, comment = "장소 FK")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_tag_id", nullable = false, comment = "분위기 태그 FK")
    private MoodTag moodTag;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_source", nullable = false, comment = "태그 출처")
    private PlaceTagSource tagSource = PlaceTagSource.OPERATOR;

    @Column(name = "confidence_score", precision = 5, scale = 4, comment = "자동 초안 신뢰도")
    private BigDecimal confidenceScore;

    @Column(name = "is_confirmed", nullable = false, comment = "검수 확정 여부")
    private Boolean isConfirmed = true;
}

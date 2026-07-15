package com.example.TODAIT__BE.domain.place.entity;

import com.example.TODAIT__BE.domain.place.enums.PlaceImageType;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "장소 이미지 PK")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false, comment = "장소 FK")
    private Place place;

    @Column(name = "image_url", nullable = false, comment = "이미지 URL")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, comment = "이미지 종류")
    private PlaceImageType imageType = PlaceImageType.OPERATOR;

    @Column(name = "source_name", comment = "이미지 출처명")
    private String sourceName;

    @Column(name = "display_order", nullable = false, comment = "노출 순서")
    private Integer displayOrder = 0;

    @Column(name = "is_primary", nullable = false, comment = "대표 이미지 여부")
    private Boolean isPrimary = false;

    @Column(name = "created_at", comment = "생성 시각")
    private LocalDateTime createdAt;
}

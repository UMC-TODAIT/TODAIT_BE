package com.example.TODAIT__BE.domain.place.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "place_source",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_place_source_data_source_source_place_id",
                columnNames = {"data_source_id", "source_place_id"}
        )
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceSource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    @Column(name = "source_place_id", nullable = false, comment = "외부 데이터 출처의 장소 고유 ID")
    private String sourcePlaceId;

    @Column(name = "source_url", comment = "외부 장소 상세 페이지 URL")
    private String sourceUrl;

    @Builder.Default
    @Column(name = "is_primary", nullable = false, comment = "대표 데이터 출처 여부")
    private Boolean isPrimary = false;
}

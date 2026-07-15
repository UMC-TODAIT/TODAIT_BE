package com.example.TODAIT__BE.domain.taxonomy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "area")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "지원 지역 PK")
    private Long id;

    @Column(nullable = false, unique = true, comment = "지역 코드")
    private String code;

    @Column(nullable = false, comment = "지역 표시명")
    private String name;

    @Column(comment = "지역 설명")
    private String description;

    @Column(name = "center_latitude", nullable = false, comment = "지역 중심 위도")
    private Double centerLatitude;

    @Column(name = "center_longitude", nullable = false, comment = "지역 중심 경도")
    private Double centerLongitude;

    @Column(name = "is_active", nullable = false, comment = "지원 여부")
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false, comment = "노출 순서")
    private Integer sortOrder = 0;
}

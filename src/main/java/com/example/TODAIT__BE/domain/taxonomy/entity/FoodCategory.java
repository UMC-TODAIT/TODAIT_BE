package com.example.TODAIT__BE.domain.taxonomy.entity;

import com.example.TODAIT__BE.global.common.BaseEntity;
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
@Table(name = "food_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "음식 카테고리 PK")
    private Long id;

    @Column(nullable = false, unique = true, comment = "음식 카테고리 코드")
    private String code;

    @Column(nullable = false, comment = "음식 카테고리 표시명")
    private String name;

    @Column(comment = "설명")
    private String description;

    @Column(name = "sort_order", nullable = false, comment = "노출 순서")
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false, comment = "사용 여부")
    private Boolean isActive = true;
}

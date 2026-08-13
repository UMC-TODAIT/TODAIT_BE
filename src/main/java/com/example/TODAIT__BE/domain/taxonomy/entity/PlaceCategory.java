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
import org.springframework.util.Assert;

@Entity
@Table(name = "place_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public static PlaceCategory of(
            String code,
            String name,
            String description,
            Integer sortOrder,
            Boolean isActive
    ) {
        // NOT NULL 컬럼은 저장 전에 검증해 실패 원인을 조기에 드러낸다. (description은 nullable)
        Assert.hasText(code, "code는 비어 있을 수 없습니다.");
        Assert.hasText(name, "name은 비어 있을 수 없습니다.");
        Assert.notNull(sortOrder, "sortOrder는 null일 수 없습니다.");
        Assert.notNull(isActive, "isActive는 null일 수 없습니다.");

        PlaceCategory placeCategory = new PlaceCategory();
        placeCategory.code = code;
        placeCategory.name = name;
        placeCategory.description = description;
        placeCategory.sortOrder = sortOrder;
        placeCategory.isActive = isActive;
        return placeCategory;
    }

    public void activate() {
        this.isActive = true;
    }
}

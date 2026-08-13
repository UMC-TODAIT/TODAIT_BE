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
@Table(name = "mood_tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "분위기 태그 PK")
    private Long id;

    @Column(nullable = false, unique = true, comment = "분위기 태그 코드")
    private String code;

    @Column(nullable = false, comment = "태그 표시명")
    private String name;

    @Column(comment = "태그 설명")
    private String description;

    @Column(name = "sort_order", nullable = false, comment = "노출 순서")
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false, comment = "사용 여부")
    private Boolean isActive = true;
}

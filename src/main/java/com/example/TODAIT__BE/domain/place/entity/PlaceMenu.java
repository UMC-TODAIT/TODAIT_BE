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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "장소 메뉴 PK")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false, comment = "장소 FK")
    private Place place;

    @Column(nullable = false, comment = "메뉴명")
    private String name;

    @Column(comment = "가격. 가격 변동 메뉴는 null 로 저장하고 프론트에서 변동으로 표시")
    private Integer price;

    @Column(name = "image_url", comment = "메뉴 이미지 URL")
    private String imageUrl;

    @Column(name = "display_order", nullable = false, comment = "노출 순서")
    private Integer displayOrder = 0;
}

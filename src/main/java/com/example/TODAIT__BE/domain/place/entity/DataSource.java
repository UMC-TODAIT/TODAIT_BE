package com.example.TODAIT__BE.domain.place.entity;

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
@Table(name = "data_source")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DataSource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", comment = "데이터 출처 PK")
    private Long id;

    @Column(nullable = false, unique = true, comment = "데이터 출처 코드 (예: KAKAO, OPERATOR)")
    private String code;

    @Column(nullable = false, comment = "데이터 출처명")
    private String name;
}

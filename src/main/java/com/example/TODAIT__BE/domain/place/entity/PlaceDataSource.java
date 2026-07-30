package com.example.TODAIT__BE.domain.place.entity;

import com.example.TODAIT__BE.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "data_source",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_data_source_code",
                columnNames = "code"
        )
)
public class PlaceDataSource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}

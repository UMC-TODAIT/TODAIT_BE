package com.example.TODAIT__BE.domain.place.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "place_source",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_place_source_data_source_external",
                columnNames = {
                        "data_source_id",
                        "source_place_id"
                }
        )
)
public class PlaceSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id", nullable = false)
    private PlaceDataSource dataSource;

    @Column(name = "source_place_id", length = 255)
    private String sourcePlaceId;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;
}

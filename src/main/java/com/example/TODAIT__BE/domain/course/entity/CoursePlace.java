package com.example.TODAIT__BE.domain.course.entity;

import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.place.entity.Place;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "course_place",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_place_course_visit_order",
                        columnNames = {"course_id", "visit_order"}
                ),
                @UniqueConstraint(
                        name = "uk_course_place_course_place",
                        columnNames = {"course_id", "place_id"}
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoursePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_role", nullable = false)
    private PlaceRole placeRole;

    @Builder.Default
    @Column(name = "is_representative", nullable = false)
    private Boolean isRepresentative = false;

    @Column(name = "place_name_snapshot", nullable = false)
    private String placeNameSnapshot;

    @Column(name = "address_snapshot", nullable = false)
    private String addressSnapshot;

    @Column(name = "latitude_snapshot", nullable = false)
    private Double latitudeSnapshot;

    @Column(name = "longitude_snapshot", nullable = false)
    private Double longitudeSnapshot;

    @Column(name = "category_snapshot", nullable = false)
    private String categorySnapshot;

    @Column
    private String memo;

    public void updateMemo(String memo) {
        this.memo = memo;
    }
}

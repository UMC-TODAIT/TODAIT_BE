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
import jakarta.persistence.Index;
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
        name = "course_draft_place",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_draft_place_draft_visit_order",
                columnNames = {"course_draft_id", "visit_order"}
        ),
        indexes = {
                @Index(
                        name = "idx_course_draft_place_draft_role",
                        columnList = "course_draft_id, place_role"
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDraftPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_draft_id", nullable = false)
    private CourseDraft courseDraft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_role", nullable = false)
    private PlaceRole placeRole;

    @Column
    private String memo;

    public void updateVisitOrder(Integer visitOrder) {
        this.visitOrder = visitOrder;
    }
}

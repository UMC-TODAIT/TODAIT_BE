package com.example.TODAIT__BE.domain.course.entity;

import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.global.common.BaseEntity;
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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "course_draft",
        indexes = {
                @Index(name = "idx_draft_member_status", columnList = "member_id, status, updated_at, id"),
                @Index(name = "idx_draft_cleanup", columnList = "status, expires_at, id")
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDraft extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseDraftStatus status;

    @Column(name = "user_latitude")
    private Double userLatitude;

    @Column(name = "user_longitude")
    private Double userLongitude;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    public static CourseDraft create(Member member) {
        return CourseDraft.builder()
                .member(member)
                .status(CourseDraftStatus.MOOD_SELECTING)
                .build();
    }

    public void changeStatus(CourseDraftStatus status) {
        this.status = status;
    }

    public void touchUpdatedAt(LocalDateTime updatedAt) {
        super.touchUpdatedAt(updatedAt);
    }

    public void completeWithCourse(Course course, LocalDateTime expiresAt) {
        this.status = CourseDraftStatus.COMPLETED;
        this.course = course;
        this.expiresAt = expiresAt;
    }

    public void abandon(LocalDateTime expiresAt) {
        this.status = CourseDraftStatus.ABANDONED;
        this.expiresAt = expiresAt;
    }
}

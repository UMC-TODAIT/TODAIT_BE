package com.example.TODAIT__BE.domain.course.entity;

import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.global.common.BaseEntity;
import jakarta.persistence.Entity;
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
        name = "course_draft_mood_tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_draft_mood_tag",
                columnNames = {"course_draft_id", "mood_tag_id"}
        )
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseDraftMoodTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_draft_id", nullable = false)
    private CourseDraft courseDraft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_tag_id", nullable = false)
    private MoodTag moodTag;
}

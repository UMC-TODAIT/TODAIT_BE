package com.example.TODAIT__BE.domain.member.entity;

import com.example.TODAIT__BE.domain.member.enums.TermType;
import com.example.TODAIT__BE.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "term",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_term_type_version",
                columnNames = {
                        "term_type",
                        "version"
                }
        )
)
public class Term extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "term_type",nullable = false)
    @Enumerated(EnumType.STRING)
    private TermType termType;

    @Column(name = "title",nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

}

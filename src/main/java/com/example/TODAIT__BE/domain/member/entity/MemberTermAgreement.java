package com.example.TODAIT__BE.domain.member.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "member_term_agreement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_term_agreement_member_term",
                columnNames = {"member_id", "term_id"}
        )
)
public class MemberTermAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="member_id",nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="term_id",nullable = false)
    private Term term;

    @Column(name = "agreed", nullable = false)
    private boolean agreed;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;
}

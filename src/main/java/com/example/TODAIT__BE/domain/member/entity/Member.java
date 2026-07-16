package com.example.TODAIT__BE.domain.member.entity;

import com.example.TODAIT__BE.domain.member.enums.MemberRole;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.global.common.BaseEntity;
import jakarta.persistence.*;
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
@Table(name="member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "role",nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberRole role = MemberRole.USER;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public void updateLastLoginAt(LocalDateTime loginAt){
        this.lastLoginAt = loginAt;
    }

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}

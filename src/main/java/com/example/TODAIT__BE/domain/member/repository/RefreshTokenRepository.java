package com.example.TODAIT__BE.domain.member.repository;

import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    List<RefreshToken> findAllByMemberAndRevokedAtIsNull(Member member);

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}

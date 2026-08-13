package com.example.TODAIT__BE.domain.member.repository;

import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    List<RefreshToken> findAllByMemberAndRevokedAtIsNull(Member member);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(
            name = "jakarta.persistence.lock.timeout",
            value = "3000"
    ))
    @Query("""
            select rt
            from RefreshToken rt
            join fetch rt.member
            where rt.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );
}

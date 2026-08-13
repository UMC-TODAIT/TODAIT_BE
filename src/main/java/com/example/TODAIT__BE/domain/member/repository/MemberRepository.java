package com.example.TODAIT__BE.domain.member.repository;

import com.example.TODAIT__BE.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(
            name = "jakarta.persistence.lock.timeout",
            value = "3000"
    ))
    @Query("select m from Member m where m.id = :memberId")
    Optional<Member> findByIdForUpdate(@Param("memberId") Long memberId);

    //이메일 로그인용
    Optional<Member> findByEmail(String email);

    //이메일 중복 확인용
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}

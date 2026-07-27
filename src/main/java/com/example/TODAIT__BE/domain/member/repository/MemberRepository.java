package com.example.TODAIT__BE.domain.member.repository;

import com.example.TODAIT__BE.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :memberId")
    Optional<Member> findByIdForUpdate(@Param("memberId") Long memberId);

    //이메일 로그인용
    Optional<Member> findByEmail(String email);

    //이메일 중복 확인용
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<Member> findByIdAndStatusAndDeletedAtIsNull(
            Long id,
            MemberStatus status
    );
}

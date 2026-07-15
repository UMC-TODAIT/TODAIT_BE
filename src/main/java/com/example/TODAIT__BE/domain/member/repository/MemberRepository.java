package com.example.TODAIT__BE.domain.member.repository;

import com.example.TODAIT__BE.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member,Long> {

    //이메일 로그인용
    Optional<Member> findByEmail(String email);

    //이메일 중복 확인용
    boolean existsByEmail(String email);

}

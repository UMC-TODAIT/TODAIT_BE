package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftCreateResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseDraftService {

    private final CourseDraftRepository courseDraftRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CourseDraftCreateResponse createCourseDraft(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new MemberException(
                                MemberErrorCode.MEMBER_NOT_FOUND
                        )
                );

        CourseDraft courseDraft = CourseDraft.create(member);

        CourseDraft savedCourseDraft =
                courseDraftRepository.save(courseDraft);

        return CourseDraftCreateResponse.from(savedCourseDraft);
    }
}

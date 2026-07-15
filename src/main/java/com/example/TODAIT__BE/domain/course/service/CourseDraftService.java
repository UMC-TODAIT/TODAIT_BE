package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftCreateResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.global.apiPayload.code.GeneralErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseDraftService {

    private final CourseDraftRepository courseDraftRepository;
    private final MemberRepository memberRepository;
    private final long expirationHours;

    public CourseDraftService(
            CourseDraftRepository courseDraftRepository,
            MemberRepository memberRepository,
            @Value("${app.course-draft.expiration-hours}") long expirationHours
    ) {
        this.courseDraftRepository = courseDraftRepository;
        this.memberRepository = memberRepository;
        this.expirationHours = expirationHours;
    }

    @Transactional
    public CourseDraftCreateResponse createCourseDraft(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.UNAUTHORIZED));

        CourseDraft courseDraft = CourseDraft.builder()
                .member(member)
                .status(CourseDraftStatus.MOOD_SELECTING)
                .expiresAt(LocalDateTime.now().plusHours(expirationHours))
                .build();

        courseDraftRepository.save(courseDraft);

        return CourseDraftCreateResponse.from(courseDraft);
    }
}

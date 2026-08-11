package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.response.MemberResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final CourseRepository courseRepository;

    public MemberResponse.NicknameAvailability checkNicknameAvailability(String nickname) {
        String normalizedNickname = MemberInputPolicy.normalizeNickname(nickname);
        boolean available = !memberRepository.existsByNickname(normalizedNickname);

        return new MemberResponse.NicknameAvailability(normalizedNickname, available);
    }

    public MemberResponse.Me getMyInfo(Long memberId) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new MemberException(MemberErrorCode.MEMBER_NOT_FOUND)
                );

        validateActiveMember(member);

        long savedCourseCount =
                courseRepository.countByMemberIdAndDeletedAtIsNull(memberId);

        return new MemberResponse.Me(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl(),
                savedCourseCount
        );
    }

    private void validateActiveMember(Member member) {
        if (member.getDeletedAt() != null) {
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }
    }
}

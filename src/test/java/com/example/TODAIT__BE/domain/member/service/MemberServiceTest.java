package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.course.repository.CourseRepository;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.response.MemberResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CourseRepository courseRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, courseRepository);
    }

    @Test
    void checkNicknameAvailabilityReturnsAvailableWhenNicknameDoesNotExist() {
        given(memberRepository.existsByNickname("tester")).willReturn(false);

        MemberResponse.NicknameAvailability response =
                memberService.checkNicknameAvailability("tester");

        assertThat(response.nickname()).isEqualTo("tester");
        assertThat(response.available()).isTrue();
    }

    @Test
    void checkNicknameAvailabilityReturnsUnavailableWhenNicknameExists() {
        given(memberRepository.existsByNickname("tester")).willReturn(true);

        MemberResponse.NicknameAvailability response =
                memberService.checkNicknameAvailability("tester");

        assertThat(response.nickname()).isEqualTo("tester");
        assertThat(response.available()).isFalse();
    }

    @Test
    void checkNicknameAvailabilityNormalizesNicknameBeforeChecking() {
        given(memberRepository.existsByNickname("tester")).willReturn(false);

        MemberResponse.NicknameAvailability response =
                memberService.checkNicknameAvailability("  tester  ");

        assertThat(response.nickname()).isEqualTo("tester");
        assertThat(response.available()).isTrue();
        verify(memberRepository).existsByNickname("tester");
    }

    @Test
    void getMyInfoReturnsMemberInfoForActiveMember() {
        Member member = Member.builder()
                .id(1L)
                .email("tester@example.com")
                .nickname("tester")
                .profileImageUrl("https://example.com/profile.png")
                .status(MemberStatus.ACTIVE)
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(courseRepository.countByMemberIdAndDeletedAtIsNull(1L)).willReturn(3L);

        MemberResponse.Me response = memberService.getMyInfo(1L);

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("tester@example.com");
        assertThat(response.nickname()).isEqualTo("tester");
        assertThat(response.profileImageUrl()).isEqualTo("https://example.com/profile.png");
        assertThat(response.savedCourseCount()).isEqualTo(3L);
    }

    @Test
    void getMyInfoThrowsNotFoundWhenMemberDoesNotExist() {
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMyInfo(1L))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
                );
    }

    @Test
    void getMyInfoThrowsNotFoundWhenMemberIsDeleted() {
        Member member = Member.builder()
                .id(1L)
                .nickname("tester")
                .status(MemberStatus.ACTIVE)
                .deletedAt(LocalDateTime.now())
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.getMyInfo(1L))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
                );
    }

    @Test
    void getMyInfoThrowsInvalidStatusWhenMemberIsNotActive() {
        Member member = Member.builder()
                .id(1L)
                .nickname("tester")
                .status(MemberStatus.BLOCKED)
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.getMyInfo(1L))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_MEMBER_STATUS)
                );
    }
}

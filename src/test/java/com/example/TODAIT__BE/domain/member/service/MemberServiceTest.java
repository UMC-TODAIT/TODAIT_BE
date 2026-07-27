package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.response.MemberNicknameResponse;
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

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    void getMyNicknameReturnsNicknameForActiveMember() {
        Member member = Member.builder()
                .id(1L)
                .nickname("tester")
                .status(MemberStatus.ACTIVE)
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        MemberNicknameResponse response = memberService.getMyNickname(1L);

        assertThat(response.nickname()).isEqualTo("tester");
    }

    @Test
    void getMyNicknameThrowsNotFoundWhenMemberDoesNotExist() {
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMyNickname(1L))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
                );
    }

    @Test
    void getMyNicknameThrowsNotFoundWhenMemberIsDeleted() {
        Member member = Member.builder()
                .id(1L)
                .nickname("tester")
                .status(MemberStatus.ACTIVE)
                .deletedAt(LocalDateTime.now())
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.getMyNickname(1L))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
                );
    }

    @Test
    void getMyNicknameThrowsInvalidStatusWhenMemberIsNotActive() {
        Member member = Member.builder()
                .id(1L)
                .nickname("tester")
                .status(MemberStatus.BLOCKED)
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.getMyNickname(1L))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.INVALID_MEMBER_STATUS)
                );
    }
}

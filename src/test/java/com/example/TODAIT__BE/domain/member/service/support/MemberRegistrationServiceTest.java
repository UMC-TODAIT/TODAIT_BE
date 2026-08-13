package com.example.TODAIT__BE.domain.member.service.support;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.entity.MemberTermAgreement;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.enums.TermType;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.exception.MemberIntegrityViolationMapper;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberTermAgreementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberRegistrationServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberOAuthAccountRepository memberOAuthAccountRepository;
    @Mock
    private MemberTermAgreementRepository memberTermAgreementRepository;
    @Mock
    private MemberIntegrityViolationMapper integrityViolationMapper;

    private MemberRegistrationService memberRegistrationService;

    @BeforeEach
    void setUp() {
        memberRegistrationService = new MemberRegistrationService(
                memberRepository,
                memberOAuthAccountRepository,
                memberTermAgreementRepository,
                integrityViolationMapper
        );
    }

    @Test
    void saveMemberDelegatesToSaveAndFlush() {
        Member member = Member.builder()
                .email("test@example.com")
                .nickname("tester")
                .build();
        given(memberRepository.saveAndFlush(member)).willReturn(member);

        Member savedMember = memberRegistrationService.saveMember(member);

        assertThat(savedMember).isEqualTo(member);
        verify(memberRepository).saveAndFlush(member);
    }

    @Test
    void saveMemberMapsDataIntegrityViolation() {
        Member member = Member.builder().nickname("tester").build();
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate");
        MemberException mappedException = new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL);
        given(memberRepository.saveAndFlush(member)).willThrow(exception);
        given(integrityViolationMapper.mapMemberSaveException(exception)).willReturn(mappedException);

        assertThatThrownBy(() -> memberRegistrationService.saveMember(member))
                .isSameAs(mappedException);
    }

    @Test
    void saveTermAgreementsBuildsAgreedRows() {
        Member member = Member.builder().id(1L).nickname("tester").build();
        Term serviceTerm = term(TermType.SERVICE);
        Term privacyTerm = term(TermType.PRIVACY);
        LocalDateTime agreedAt = LocalDateTime.of(2026, 7, 26, 3, 0);

        memberRegistrationService.saveTermAgreements(
                member,
                List.of(serviceTerm, privacyTerm),
                agreedAt
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<MemberTermAgreement>> captor =
                (ArgumentCaptor<Iterable<MemberTermAgreement>>) (ArgumentCaptor<?>)
                        ArgumentCaptor.forClass(Iterable.class);
        verify(memberTermAgreementRepository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .hasSize(2)
                .allSatisfy(agreement -> {
                    assertThat(agreement.getMember()).isEqualTo(member);
                    assertThat(agreement.isAgreed()).isTrue();
                    assertThat(agreement.getAgreedAt()).isEqualTo(agreedAt);
                })
                .extracting(MemberTermAgreement::getTerm)
                .containsExactly(serviceTerm, privacyTerm);
    }

    @Test
    void saveOAuthAccountBuildsAndFlushesAccount() {
        Member member = Member.builder().id(1L).nickname("tester").build();
        LocalDateTime linkedAt = LocalDateTime.of(2026, 7, 26, 3, 10);
        given(memberOAuthAccountRepository.saveAndFlush(any(MemberOAuthAccount.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        MemberOAuthAccount savedAccount = memberRegistrationService.saveOAuthAccount(
                member,
                OAuthProvider.GOOGLE,
                "provider-user-id",
                "user@example.com",
                linkedAt
        );

        assertThat(savedAccount.getMember()).isEqualTo(member);
        assertThat(savedAccount.getProvider()).isEqualTo(OAuthProvider.GOOGLE);
        assertThat(savedAccount.getProviderUserId()).isEqualTo("provider-user-id");
        assertThat(savedAccount.getProviderEmail()).isEqualTo("user@example.com");
        assertThat(savedAccount.getLinkedAt()).isEqualTo(linkedAt);
    }

    @Test
    void saveOAuthAccountMapsDataIntegrityViolation() {
        Member member = Member.builder().id(1L).nickname("tester").build();
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate");
        MemberException mappedException =
                new MemberException(MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT);

        given(memberOAuthAccountRepository.saveAndFlush(any(MemberOAuthAccount.class))).willThrow(exception);
        given(integrityViolationMapper.mapOAuthAccountSaveException(exception)).willReturn(mappedException);

        assertThatThrownBy(() -> memberRegistrationService.saveOAuthAccount(
                member,
                OAuthProvider.GOOGLE,
                "provider-user-id",
                "user@example.com",
                LocalDateTime.now()
        )).isSameAs(mappedException);
    }

    private Term term(TermType termType) {
        return Term.builder()
                .termType(termType)
                .title(termType.name())
                .content("content")
                .version("1.0")
                .isRequired(true)
                .isActive(true)
                .build();
    }
}

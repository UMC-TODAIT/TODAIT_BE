package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.SignRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.infra.redis.EmailVerificationRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRedisRepository emailVerificationRedisRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final TermAgreementValidator termAgreementValidator;
    private final MemberRegistrationService memberRegistrationService;

    @Transactional
    public AuthTokenResponse.Token signup(
            SignRequest.SignUp request
    ){
        validateEmailVerification(request.email());
        validateMemberDuplicate(request.email(), request.nickname());

        List<Term> agreedTerms =
                termAgreementValidator.validateAndGetAgreedTerms(
                        request.termAgreements()
                );
        String passwordHash = passwordEncoder.encode(request.password());

        LocalDateTime now = LocalDateTime.now();

        Member savedMember = memberRegistrationService.saveMember(
                Member.builder()
                        .email(request.email())
                        .nickname(request.nickname())
                        .passwordHash(passwordHash)
                        .build()
        );
        memberRegistrationService.saveTermAgreements(savedMember, agreedTerms, now);

        return authService.issueTokens(savedMember);
    }

    private void validateEmailVerification(String email){
        if(!emailVerificationRedisRepository.isVerified(email)){
            throw new MemberException(
                    MemberErrorCode.EMAIL_VERIFICATION_REQUIRED
            );
        }
    }

    private void validateMemberDuplicate(String email, String nickname){
        if(memberRepository.existsByEmail(email)){
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL);
        }
        if(memberRepository.existsByNickname(nickname)){
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_NICKNAME);
        }
    }
}

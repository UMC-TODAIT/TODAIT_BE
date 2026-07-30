package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
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

    private final EmailVerificationRedisRepository emailVerificationRedisRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final TermAgreementValidator termAgreementValidator;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberDuplicateValidator memberDuplicateValidator;

    @Transactional
    public AuthResponse.Token signup(
            AuthRequest.SignUp request
    ){
        validateEmailVerification(request.email());
        memberDuplicateValidator.validateEmailAvailable(request.email());
        memberDuplicateValidator.validateNicknameAvailable(request.nickname());

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

}

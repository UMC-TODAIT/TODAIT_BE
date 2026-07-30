package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.service.validator.MemberLoginValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailLoginService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final MemberLoginValidator memberLoginValidator;

    @Transactional
    public AuthResponse.Token login(
            AuthRequest.Login request
    ){
        Member member = findMember(request.email());

        validatePassword(
                request.password(),
                member.getPasswordHash()
        );

        memberLoginValidator.validateLoginAvailable(member);

        return authService.issueTokens(member);
    }

    private Member findMember(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(()-> new MemberException(MemberErrorCode.INVALID_EMAIL_OR_PASSWORD));
    }

    private void validatePassword(
            String rawPassword,
            String passwordHash
    ){
        if(passwordHash == null || !passwordEncoder.matches(rawPassword,passwordHash)){
            throw new MemberException(MemberErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }
    }

}

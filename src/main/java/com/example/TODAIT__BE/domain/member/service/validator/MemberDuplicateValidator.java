package com.example.TODAIT__BE.domain.member.service.validator;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberDuplicateValidator {

    private final MemberRepository memberRepository;
    private final MemberOAuthAccountRepository memberOAuthAccountRepository;

    public void validateEmailAvailable(String email) {
        if (email != null && memberRepository.existsByEmail(email)) {
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL);
        }
    }

    public void validateNicknameAvailable(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_NICKNAME);
        }
    }

    public void validateOAuthAccountAvailable(
            OAuthProvider provider,
            String providerUserId
    ) {
        if (memberOAuthAccountRepository.existsByProviderAndProviderUserId(provider, providerUserId)) {
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_OAUTH_ACCOUNT);
        }
    }
}

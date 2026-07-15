package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthLoginResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exeption.MemberException;
import com.example.TODAIT__BE.domain.member.exeption.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final MemberOAuthAccountRepository memberOAuthAccountRepository;
    private final AuthService authService;
    private final MemberRepository memberRepository;
    public OAuthLoginResponse.OAuthLogin loginWithKakao(KakaoUserInfo kakaoUserInfo) {
        OAuthProvider provider = OAuthProvider.KAKAO;
        String providerUserId = kakaoUserInfo.providerUserId();

        Optional<MemberOAuthAccount> result = memberOAuthAccountRepository.findByProviderAndProviderUserId(provider,providerUserId);

        if(result.isPresent()) {
            Member member = result.get().getMember();
            return loginExistingMember(member, provider);

        }
        if(kakaoUserInfo.email() != null && memberRepository.existsByEmail(kakaoUserInfo.email())){
            throw new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL);
        }
        return requireOnboarding(provider, kakaoUserInfo);
    }

    //기존 회원 처리
    private OAuthLoginResponse.OAuthLogin loginExistingMember(Member member, OAuthProvider provider) {
        validateLoginAvailable(member);

        AuthTokenResponse.Token tokenResponse = authService.issueTokens(member);

        return OAuthLoginResponse.OAuthLogin.builder()
                .loginStatus("LOGIN_COMPLETED")
                .accessToken(tokenResponse.accessToken())
                .refreshToken(tokenResponse.refreshToken())
                .onboardingToken(null)
                .email(member.getEmail())
                .provider(provider)
                .build();
    }

    //신규 회원 처리
    private OAuthLoginResponse.OAuthLogin requireOnboarding(
            OAuthProvider provider,
            KakaoUserInfo kakaoUserInfo
    ) {
        String onboardingToken = authService.issueOAuthOnboardingToken(
                provider,
                kakaoUserInfo.providerUserId(),
                kakaoUserInfo.email()
        );

        return OAuthLoginResponse.OAuthLogin.builder()
                .loginStatus("ONBOARDING_REQUIRED")
                .accessToken(null)
                .refreshToken(null)
                .onboardingToken(onboardingToken)
                .email(kakaoUserInfo.email())
                .provider(provider)
                .build();
    }

    private void validateLoginAvailable(Member member) {

        if(member.getStatus() != MemberStatus.ACTIVE) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }

    }
}

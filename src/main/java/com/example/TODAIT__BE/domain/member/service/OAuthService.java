package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.dto.response.AuthTokenResponse;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthLoginResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.exception.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.infra.oauth.dto.GoogleUserInfo;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final MemberOAuthAccountRepository memberOAuthAccountRepository;
    private final AuthService authService;
    private final MemberRepository memberRepository;

    public OAuthLoginResponse.OAuthLogin loginWithKakao(
            KakaoUserInfo kakaoUserInfo
    ) {
        return loginWithOAuth(
                OAuthProvider.KAKAO,
                kakaoUserInfo.providerUserId(),
                kakaoUserInfo.email()
        );
    }

    public OAuthLoginResponse.OAuthLogin loginWithGoogle(
            GoogleUserInfo googleUserInfo
    ){
        return loginWithOAuth(
                OAuthProvider.GOOGLE,
                googleUserInfo.providerUserId(),
                googleUserInfo.email()
        );
    }

    private OAuthLoginResponse.OAuthLogin loginWithOAuth(
            OAuthProvider provider,
            String providerUserId,
            String email
    ){
        Optional<MemberOAuthAccount> result = memberOAuthAccountRepository.findByProviderAndProviderUserId(
                provider,
                providerUserId
        );

        if( result.isPresent()){
            Member member = result.get().getMember();
            return loginExistingMember(member,provider);
        }

        if(email != null && memberRepository.existsByEmail(email)){
            throw  new MemberException(
                    MemberErrorCode.ALREADY_REGISTERED_EMAIL
            );
        }

        return requireOnboarding(
                provider,
                providerUserId,
                email
        );
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
            String providerUserId,
            String email
    ) {
        String onboardingToken = authService.issueOAuthOnboardingToken(
                provider,
                providerUserId,
                email
        );

        return OAuthLoginResponse.OAuthLogin.builder()
                .loginStatus("ONBOARDING_REQUIRED")
                .accessToken(null)
                .refreshToken(null)
                .onboardingToken(onboardingToken)
                .email(email)
                .provider(provider)
                .build();
    }

    private void validateLoginAvailable(Member member) {

        if(member.getStatus() != MemberStatus.ACTIVE) {
            throw new MemberException(MemberErrorCode.INVALID_MEMBER_STATUS);
        }

    }
}

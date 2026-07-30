package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import com.example.TODAIT__BE.infra.oauth.GoogleOAuthClient;
import com.example.TODAIT__BE.infra.oauth.KakaoOAuthClient;
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
    private final KakaoOAuthClient kakaoOAuthClient;
    private final GoogleOAuthClient googleOAuthClient;
    private final MemberLoginValidator memberLoginValidator;
    private final MemberDuplicateValidator memberDuplicateValidator;

    public OAuthResponse.Login loginWithKakao(
            String accessToken
    ) {
        KakaoUserInfo userInfo = kakaoOAuthClient.getUserInfo(accessToken);
        return loginWithOAuth(
                OAuthProvider.KAKAO,
                userInfo.providerUserId(),
                userInfo.email()
        );
    }

    public OAuthResponse.Login loginWithGoogle(
            String idToken
    ){
        GoogleUserInfo userInfo = googleOAuthClient.verifyIdToken(idToken);
        return loginWithOAuth(
                OAuthProvider.GOOGLE,
                userInfo.providerUserId(),
                userInfo.email()
        );
    }

    private OAuthResponse.Login loginWithOAuth(
            OAuthProvider provider,
            String providerUserId,
            String email
    ){
        String normalizedEmail = MemberInputPolicy.normalizeEmail(email);
        Optional<MemberOAuthAccount> result = memberOAuthAccountRepository.findByProviderAndProviderUserId(
                provider,
                providerUserId
        );

        if( result.isPresent()){
            Member member = result.get().getMember();
            return loginExistingMember(member,provider);
        }

        memberDuplicateValidator.validateEmailAvailable(normalizedEmail);

        return requireOnboarding(
                provider,
                providerUserId,
                normalizedEmail
        );
    }

    //기존 회원 처리
    private OAuthResponse.Login loginExistingMember(Member member, OAuthProvider provider) {
        memberLoginValidator.validateLoginAvailable(member);

        AuthResponse.Token tokenResponse = authService.issueTokens(member);

        return OAuthResponse.Login.builder()
                .loginStatus("LOGIN_COMPLETED")
                .accessToken(tokenResponse.accessToken())
                .refreshToken(tokenResponse.refreshToken())
                .onboardingToken(null)
                .email(member.getEmail())
                .provider(provider)
                .build();
    }

    //신규 회원 처리
    private OAuthResponse.Login requireOnboarding(
            OAuthProvider provider,
            String providerUserId,
            String email
    ) {
        String onboardingToken = authService.issueOAuthOnboardingToken(
                provider,
                providerUserId,
                email
        );

        return OAuthResponse.Login.builder()
                .loginStatus("ONBOARDING_REQUIRED")
                .accessToken(null)
                .refreshToken(null)
                .onboardingToken(onboardingToken)
                .email(email)
                .provider(provider)
                .build();
    }

}

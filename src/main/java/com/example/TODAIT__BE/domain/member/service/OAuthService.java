package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserClient;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserInfo;
import com.example.TODAIT__BE.domain.member.service.validator.MemberDuplicateValidator;
import com.example.TODAIT__BE.domain.member.service.validator.MemberLoginValidator;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class OAuthService {

    private final MemberOAuthAccountRepository memberOAuthAccountRepository;
    private final AuthService authService;
    private final Map<OAuthProvider, OAuthUserClient> oAuthUserClients;
    private final MemberLoginValidator memberLoginValidator;
    private final MemberDuplicateValidator memberDuplicateValidator;

    public OAuthService(
            MemberOAuthAccountRepository memberOAuthAccountRepository,
            AuthService authService,
            List<OAuthUserClient> oAuthUserClients,
            MemberLoginValidator memberLoginValidator,
            MemberDuplicateValidator memberDuplicateValidator
    ) {
        this.memberOAuthAccountRepository = memberOAuthAccountRepository;
        this.authService = authService;
        this.oAuthUserClients = mapOAuthUserClients(oAuthUserClients);
        this.memberLoginValidator = memberLoginValidator;
        this.memberDuplicateValidator = memberDuplicateValidator;
    }

    public OAuthResponse.Login loginWithKakao(
            String accessToken
    ) {
        OAuthUserInfo userInfo = getOAuthUserInfo(OAuthProvider.KAKAO, accessToken);
        return loginWithOAuth(
                OAuthProvider.KAKAO,
                userInfo.providerUserId(),
                userInfo.email()
        );
    }

    public OAuthResponse.Login loginWithGoogle(
            String idToken
    ){
        OAuthUserInfo userInfo = getOAuthUserInfo(OAuthProvider.GOOGLE, idToken);
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

    private OAuthUserInfo getOAuthUserInfo(OAuthProvider provider, String token) {
        OAuthUserClient oAuthUserClient = oAuthUserClients.get(provider);
        if (oAuthUserClient == null) {
            throw new IllegalStateException("OAuth client is not configured. provider=" + provider);
        }
        return oAuthUserClient.getUserInfo(token);
    }

    private Map<OAuthProvider, OAuthUserClient> mapOAuthUserClients(
            List<OAuthUserClient> oAuthUserClients
    ) {
        Map<OAuthProvider, OAuthUserClient> result = new EnumMap<>(OAuthProvider.class);
        for (OAuthUserClient oAuthUserClient : oAuthUserClients) {
            OAuthProvider provider = Objects.requireNonNull(
                    oAuthUserClient.supports(),
                    "OAuth client provider must not be null."
            );
            if (result.put(provider, oAuthUserClient) != null) {
                throw new IllegalStateException(
                        "Duplicate OAuth client configured. provider=" + provider
                );
            }
        }

        for (OAuthProvider provider : OAuthProvider.values()) {
            if (!result.containsKey(provider)) {
                throw new IllegalStateException(
                        "OAuth client is not configured. provider=" + provider
                );
            }
        }

        return Map.copyOf(result);
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

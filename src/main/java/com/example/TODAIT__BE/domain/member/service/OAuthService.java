package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.dto.request.OAuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.dto.response.OAuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.MemberOAuthAccount;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.repository.MemberOAuthAccountRepository;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserClient;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserInfo;
import com.example.TODAIT__BE.domain.member.service.support.MemberRegistrationService;
import com.example.TODAIT__BE.domain.member.service.validator.MemberDuplicateValidator;
import com.example.TODAIT__BE.domain.member.service.validator.MemberLoginValidator;
import com.example.TODAIT__BE.domain.member.service.validator.TermAgreementValidator;
import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final TermAgreementValidator termAgreementValidator;
    private final MemberRegistrationService memberRegistrationService;

    public OAuthService(
            MemberOAuthAccountRepository memberOAuthAccountRepository,
            AuthService authService,
            List<OAuthUserClient> oAuthUserClients,
            MemberLoginValidator memberLoginValidator,
            MemberDuplicateValidator memberDuplicateValidator,
            TermAgreementValidator termAgreementValidator,
            MemberRegistrationService memberRegistrationService
    ) {
        this.memberOAuthAccountRepository = memberOAuthAccountRepository;
        this.authService = authService;
        this.oAuthUserClients = mapOAuthUserClients(oAuthUserClients);
        this.memberLoginValidator = memberLoginValidator;
        this.memberDuplicateValidator = memberDuplicateValidator;
        this.termAgreementValidator = termAgreementValidator;
        this.memberRegistrationService = memberRegistrationService;
    }

    public OAuthResponse.Login loginWithKakao(
            String accessToken
    ) {
        OAuthUserInfo userInfo = getOAuthUserInfo(OAuthProvider.KAKAO, accessToken);
        return loginWithOAuth(
                OAuthProvider.KAKAO,
                userInfo.providerUserId(),
                userInfo.email(),
                userInfo.profileImageUrl()
        );
    }

    public OAuthResponse.Login loginWithGoogle(
            String idToken
    ){
        OAuthUserInfo userInfo = getOAuthUserInfo(OAuthProvider.GOOGLE, idToken);
        return loginWithOAuth(
                OAuthProvider.GOOGLE,
                userInfo.providerUserId(),
                userInfo.email(),
                userInfo.profileImageUrl()
        );
    }

    @Transactional
    public AuthResponse.Token completeOnboarding(
            String onboardingToken,
            OAuthRequest.Onboarding request
    ){
        AuthService.OAuthOnboardingTokenClaims claims =
                authService.validateOAuthOnboardingToken(onboardingToken);
        String providerUserId = claims.providerUserId();
        OAuthProvider provider = claims.provider();
        String email = MemberInputPolicy.normalizeEmail(
                claims.email()
        );
        String nickname = request.nickname();

        memberDuplicateValidator.validateNicknameAvailable(nickname);
        memberDuplicateValidator.validateOAuthAccountAvailable(provider, providerUserId);
        memberDuplicateValidator.validateEmailAvailable(email);

        List<Term> agreedTerms =
                termAgreementValidator.validateAndGetAgreedTerms(
                        request.termAgreements()
                );

        LocalDateTime now = LocalDateTime.now();

        Member savedMember = memberRegistrationService.saveMember(
                Member.builder()
                        .email(email)
                        .nickname(nickname)
                        .profileImageUrl(claims.profileImageUrl())
                        .build()
        );

        memberRegistrationService.saveOAuthAccount(
                savedMember,
                provider,
                providerUserId,
                email,
                now
        );

        memberRegistrationService.saveTermAgreements(savedMember, agreedTerms, now);

        return authService.issueTokens(savedMember);
    }

    private OAuthResponse.Login loginWithOAuth(
            OAuthProvider provider,
            String providerUserId,
            String email,
            String profileImageUrl
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
                normalizedEmail,
                profileImageUrl
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
            String email,
            String profileImageUrl
    ) {
        String onboardingToken = authService.issueOAuthOnboardingToken(
                provider,
                providerUserId,
                email,
                profileImageUrl
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

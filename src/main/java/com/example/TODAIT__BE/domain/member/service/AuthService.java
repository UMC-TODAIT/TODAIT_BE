package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.member.repository.RefreshTokenRepository;
import com.example.TODAIT__BE.domain.member.service.port.EmailVerificationStore;
import com.example.TODAIT__BE.domain.member.service.support.MemberRegistrationService;
import com.example.TODAIT__BE.domain.member.service.validator.MemberDuplicateValidator;
import com.example.TODAIT__BE.domain.member.service.validator.MemberLoginValidator;
import com.example.TODAIT__BE.domain.member.service.validator.RefreshTokenValidator;
import com.example.TODAIT__BE.domain.member.service.validator.TermAgreementValidator;
import com.example.TODAIT__BE.global.security.token.JwtTokenProvider;
import com.example.TODAIT__BE.global.security.token.RefreshTokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final RefreshTokenHasher refreshTokenHasher;
    private final EmailVerificationStore emailVerificationStore;
    private final PasswordEncoder passwordEncoder;
    private final TermAgreementValidator termAgreementValidator;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberDuplicateValidator memberDuplicateValidator;
    private final MemberLoginValidator memberLoginValidator;
    private final RefreshTokenValidator refreshTokenValidator;

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

        return issueTokens(savedMember);
    }

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

        return issueTokens(member);
    }

    @Transactional
    public void logout(AuthRequest.Logout request) {
        RefreshToken storedToken = refreshTokenValidator
                .validateAndGetStoredTokenForUpdate(request.refreshToken());
        storedToken.revoke();
    }

    @Transactional
    public AuthResponse.Token refresh(
            AuthRequest.TokenRefresh request
    ){
        RefreshToken storedToken = refreshTokenValidator
                .validateAndGetStoredTokenForUpdate(request.refreshToken());
        Member member = storedToken.getMember();

        memberLoginValidator.validateLoginAvailable(member);

        storedToken.revoke();

        return createAndStoreTokenPair(member, LocalDateTime.now());
    }

    @Transactional
    public AuthResponse.Token issueTokens(Member member){

        Member managedMember = memberRepository.findByIdForUpdate(member.getId())
                .orElseThrow(() -> new IllegalStateException("토큰 발급 대상 회원을 찾을 수 없습니다."));

        LocalDateTime issuedAt = LocalDateTime.now();

        managedMember.updateLastLoginAt(issuedAt);

        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByMemberAndRevokedAtIsNull(managedMember);
        activeTokens.forEach(RefreshToken::revoke);

        return createAndStoreTokenPair(managedMember, issuedAt);
    }

    private AuthResponse.Token createAndStoreTokenPair(
            Member member,
            LocalDateTime issuedAt
    ) {
        String accessToken = jwtTokenProvider.createAccessToken(
                member.getId(),
                member.getRole()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        String refreshTokenHash = refreshTokenHasher.hash(refreshToken);

        LocalDateTime expiresAt = issuedAt.plus(
                Duration.ofMillis(
                        jwtTokenProvider.getRefreshTokenExpiration()
                )
        );

        RefreshToken newRefreshToken = RefreshToken.builder()
                .member(member)
                .tokenHash(refreshTokenHash)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return AuthResponse.Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String issueOAuthOnboardingToken(
            OAuthProvider provider,
            String providerUserId,
            String email,
            String profileImageUrl
    ) {
        return jwtTokenProvider.createOAuthOnboardingToken(
                provider,
                providerUserId,
                email,
                profileImageUrl
        );
    }

    public OAuthOnboardingTokenClaims validateOAuthOnboardingToken(String onboardingToken) {
        if (!jwtTokenProvider.validateToken(onboardingToken)
                || !jwtTokenProvider.isOAuthOnboardingToken(onboardingToken)) {
            throw new MemberException(MemberErrorCode.INVALID_ONBOARDING_TOKEN);
        }

        return new OAuthOnboardingTokenClaims(
                jwtTokenProvider.getOAuthProvider(onboardingToken),
                jwtTokenProvider.getSubject(onboardingToken),
                jwtTokenProvider.getEmail(onboardingToken),
                jwtTokenProvider.getProfileImageUrl(onboardingToken)
        );
    }

    public record OAuthOnboardingTokenClaims(
            OAuthProvider provider,
            String providerUserId,
            String email,
            String profileImageUrl
    ) {
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

    private void validateEmailVerification(String email){
        if(!emailVerificationStore.isVerified(email)){
            throw new MemberException(
                    MemberErrorCode.EMAIL_VERIFICATION_REQUIRED
            );
        }
    }

}

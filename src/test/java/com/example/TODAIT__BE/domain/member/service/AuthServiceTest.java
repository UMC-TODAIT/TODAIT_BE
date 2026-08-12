package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.dto.request.TermAgreementRequest;
import com.example.TODAIT__BE.domain.member.dto.response.AuthResponse;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.entity.Term;
import com.example.TODAIT__BE.domain.member.enums.MemberStatus;
import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.enums.TermType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.activeMember;
import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.member;
import static com.example.TODAIT__BE.domain.member.service.MemberServiceTestFixtures.refreshToken;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RefreshTokenHasher refreshTokenHasher;
    @Mock
    private EmailVerificationStore emailVerificationStore;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TermAgreementValidator termAgreementValidator;
    @Mock
    private MemberRegistrationService memberRegistrationService;
    @Mock
    private MemberDuplicateValidator memberDuplicateValidator;
    @Mock
    private MemberLoginValidator memberLoginValidator;
    @Mock
    private RefreshTokenValidator refreshTokenValidator;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                jwtTokenProvider,
                refreshTokenRepository,
                memberRepository,
                refreshTokenHasher,
                emailVerificationStore,
                passwordEncoder,
                termAgreementValidator,
                memberRegistrationService,
                memberDuplicateValidator,
                memberLoginValidator,
                refreshTokenValidator
        );
    }

    @Test
    void signupNormalizesInputAndIssuesTokens() {
        AuthRequest.SignUp request = signupRequest(" Tester@Example.com ", " tester ");
        List<Term> agreedTerms = List.of(term(TermType.SERVICE));
        Member savedMember = Member.builder()
                .id(1L)
                .email("tester@example.com")
                .nickname("tester")
                .passwordHash("encoded-password")
                .build();

        given(emailVerificationStore.isVerified("tester@example.com")).willReturn(true);
        given(termAgreementValidator.validateAndGetAgreedTerms(request.termAgreements()))
                .willReturn(agreedTerms);
        given(passwordEncoder.encode("password!1")).willReturn("encoded-password");
        given(memberRegistrationService.saveMember(any(Member.class))).willReturn(savedMember);
        givenIssueTokens(savedMember);

        AuthResponse.Token response = authService.signup(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRegistrationService).saveMember(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getEmail()).isEqualTo("tester@example.com");
        assertThat(memberCaptor.getValue().getNickname()).isEqualTo("tester");
        assertThat(memberCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        verify(memberRegistrationService).saveTermAgreements(
                eq(savedMember),
                eq(agreedTerms),
                any(LocalDateTime.class)
        );
    }

    @Test
    void signupRejectsUnverifiedEmail() {
        AuthRequest.SignUp request = signupRequest("test@example.com", "tester");
        given(emailVerificationStore.isVerified("test@example.com")).willReturn(false);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.EMAIL_VERIFICATION_REQUIRED);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void signupRejectsDuplicateEmail() {
        AuthRequest.SignUp request = signupRequest("test@example.com", "tester");
        given(emailVerificationStore.isVerified("test@example.com")).willReturn(true);
        willThrow(new MemberException(MemberErrorCode.ALREADY_REGISTERED_EMAIL))
                .given(memberDuplicateValidator)
                .validateEmailAvailable("test@example.com");

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_EMAIL);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void signupRejectsDuplicateNickname() {
        AuthRequest.SignUp request = signupRequest("test@example.com", "tester");
        given(emailVerificationStore.isVerified("test@example.com")).willReturn(true);
        willThrow(new MemberException(MemberErrorCode.ALREADY_REGISTERED_NICKNAME))
                .given(memberDuplicateValidator)
                .validateNicknameAvailable("tester");

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.ALREADY_REGISTERED_NICKNAME);

        verify(memberRegistrationService, never()).saveMember(any());
    }

    @Test
    void loginNormalizesEmailAndIssuesTokens() {
        AuthRequest.Login request = new AuthRequest.Login(" User@Example.com ", "password!1");
        Member member = Member.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("user")
                .passwordHash("encoded-password")
                .status(MemberStatus.ACTIVE)
                .build();

        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("password!1", "encoded-password")).willReturn(true);
        givenIssueTokens(member);

        AuthResponse.Token response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(memberLoginValidator).validateLoginAvailable(member);
    }

    @Test
    void loginRejectsUnknownEmail() {
        AuthRequest.Login request = new AuthRequest.Login("unknown@example.com", "password!1");
        given(memberRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }

    @Test
    void loginRejectsWrongPassword() {
        AuthRequest.Login request = new AuthRequest.Login("user@example.com", "wrong-password");
        Member member = Member.builder()
                .email("user@example.com")
                .nickname("user")
                .passwordHash("encoded-password")
                .build();

        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }

    @Test
    void loginRejectsSocialOnlyMember() {
        AuthRequest.Login request = new AuthRequest.Login("social@example.com", "password!1");
        Member member = Member.builder()
                .email("social@example.com")
                .nickname("social")
                .passwordHash(null)
                .build();

        given(memberRepository.findByEmail("social@example.com")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_EMAIL_OR_PASSWORD);
    }

    @Test
    void logoutRevokesValidRefreshToken() {
        AuthRequest.Logout request = new AuthRequest.Logout("refresh-token");
        RefreshToken storedToken = refreshToken(activeMember(1L), "refresh-token-hash", LocalDateTime.now().plusHours(1));

        given(refreshTokenValidator.validateAndGetStoredToken(request.refreshToken()))
                .willReturn(storedToken);

        authService.logout(request);

        assertThat(storedToken.isRevoked()).isTrue();
    }

    @Test
    void logoutPropagatesRefreshTokenValidationFailure() {
        AuthRequest.Logout request = new AuthRequest.Logout("invalid-refresh-token");

        given(refreshTokenValidator.validateAndGetStoredToken(request.refreshToken()))
                .willThrow(new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        assertThatThrownBy(() -> authService.logout(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshRotatesValidRefreshToken() {
        AuthRequest.TokenRefresh request = new AuthRequest.TokenRefresh("refresh-token");
        Member member = activeMember(1L);
        RefreshToken storedToken = refreshToken(member, "refresh-token-hash", LocalDateTime.now().plusHours(1));

        given(refreshTokenValidator.validateAndGetStoredTokenForUpdate(request.refreshToken()))
                .willReturn(storedToken);
        given(jwtTokenProvider.createAccessToken(member.getId(), member.getRole()))
                .willReturn("new-access-token");
        given(jwtTokenProvider.createRefreshToken(member.getId()))
                .willReturn("new-refresh-token");
        given(refreshTokenHasher.hash("new-refresh-token"))
                .willReturn("new-refresh-token-hash");
        given(jwtTokenProvider.getRefreshTokenExpiration()).willReturn(3600000L);

        AuthResponse.Token response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(storedToken.isRevoked()).isTrue();

        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getMember()).isSameAs(member);
        assertThat(tokenCaptor.getValue().getTokenHash())
                .isEqualTo("new-refresh-token-hash");

        verify(memberLoginValidator).validateLoginAvailable(member);
        verify(refreshTokenRepository, never())
                .findAllByMemberAndRevokedAtIsNull(any(Member.class));
    }

    @Test
    void refreshPropagatesRefreshTokenValidationFailure() {
        AuthRequest.TokenRefresh request = new AuthRequest.TokenRefresh("invalid-refresh-token");

        given(refreshTokenValidator.validateAndGetStoredTokenForUpdate(request.refreshToken()))
                .willThrow(new MemberException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshRejectsInactiveMember() {
        AuthRequest.TokenRefresh request = new AuthRequest.TokenRefresh("refresh-token");
        Member blockedMember = member(1L, "blocked", MemberStatus.BLOCKED);
        RefreshToken storedToken = refreshToken(blockedMember, "refresh-token-hash", LocalDateTime.now().plusHours(1));

        given(refreshTokenValidator.validateAndGetStoredTokenForUpdate(request.refreshToken()))
                .willReturn(storedToken);
        willThrow(new MemberException(MemberErrorCode.INVALID_MEMBER_STATUS))
                .given(memberLoginValidator)
                .validateLoginAvailable(blockedMember);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_MEMBER_STATUS);
        assertThat(storedToken.isRevoked()).isFalse();
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void validateOAuthOnboardingTokenReturnsClaims() {
        given(jwtTokenProvider.validateToken("token")).willReturn(true);
        given(jwtTokenProvider.isOAuthOnboardingToken("token")).willReturn(true);
        given(jwtTokenProvider.getOAuthProvider("token")).willReturn(OAuthProvider.GOOGLE);
        given(jwtTokenProvider.getSubject("token")).willReturn("provider-user-id");
        given(jwtTokenProvider.getEmail("token")).willReturn("user@example.com");

        AuthService.OAuthOnboardingTokenClaims claims =
                authService.validateOAuthOnboardingToken("token");

        assertThat(claims.provider()).isEqualTo(OAuthProvider.GOOGLE);
        assertThat(claims.providerUserId()).isEqualTo("provider-user-id");
        assertThat(claims.email()).isEqualTo("user@example.com");
    }

    @Test
    void validateOAuthOnboardingTokenRejectsInvalidToken() {
        given(jwtTokenProvider.validateToken("token")).willReturn(false);

        assertThatThrownBy(() -> authService.validateOAuthOnboardingToken("token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_ONBOARDING_TOKEN);
    }

    @Test
    void validateOAuthOnboardingTokenRejectsNonOnboardingToken() {
        given(jwtTokenProvider.validateToken("token")).willReturn(true);
        given(jwtTokenProvider.isOAuthOnboardingToken("token")).willReturn(false);

        assertThatThrownBy(() -> authService.validateOAuthOnboardingToken("token"))
                .isInstanceOf(MemberException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.INVALID_ONBOARDING_TOKEN);
    }

    private void givenIssueTokens(Member member) {
        given(memberRepository.findByIdForUpdate(member.getId())).willReturn(Optional.of(member));
        given(jwtTokenProvider.createAccessToken(member.getId(), member.getRole()))
                .willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(member.getId()))
                .willReturn("refresh-token");
        given(refreshTokenHasher.hash("refresh-token")).willReturn("refresh-token-hash");
        given(refreshTokenRepository.findAllByMemberAndRevokedAtIsNull(member))
                .willReturn(List.of());
        given(jwtTokenProvider.getRefreshTokenExpiration()).willReturn(3600000L);
    }

    private AuthRequest.SignUp signupRequest(String email, String nickname) {
        return new AuthRequest.SignUp(
                nickname,
                email,
                "password!1",
                List.of(new TermAgreementRequest(TermType.SERVICE, true))
        );
    }

    private Term term(TermType termType) {
        return Term.builder()
                .termType(termType)
                .title(termType.name())
                .content("content")
                .version("1.0")
                .isRequired(true)
                .isActive(true)
                .build();
    }
}

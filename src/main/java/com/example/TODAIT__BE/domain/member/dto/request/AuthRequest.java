package com.example.TODAIT__BE.domain.member.dto.request;

import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AuthRequest {

    private AuthRequest() {
    }

    @Schema(name = "AuthSignUpRequest")
    public record SignUp(
            @Schema(
                    description = "서비스에서 사용할 닉네임, 2~12자의 한글·영문·숫자",
                    example = "투데잇테스트"
            )
            @NotBlank
            @Size(min = 2, max = 12)
            @Pattern(
                    regexp = "^[가-힣a-zA-Z0-9]+$",
                    message = "닉네임에는 한글, 영문, 숫자만 사용할 수 있습니다."
            )
            String nickname,

            @Schema(
                    description = "이메일 인증을 완료한 회원 이메일",
                    example = "user@example.com"
            )
            @NotBlank
            @Email
            String email,

            @Schema(
                    description = "영문, 숫자, 특수문자를 포함한 8~72자의 비밀번호",
                    example = "Todait1234!"
            )
            @NotBlank
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,72}$",
                    message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다."
            )
            String password,

            @Schema(
                    description = "약관 유형별 동의 정보"
            )
            @NotEmpty
            List<@Valid TermAgreementRequest> termAgreements
    ) {
        public SignUp {
            nickname = MemberInputPolicy.normalizeNickname(nickname);
            email = MemberInputPolicy.normalizeEmail(email);
        }
    }

    @Schema(name = "AuthLoginRequest")
    public record Login(
            @Schema(
                    description = "가입한 회원 이메일",
                    example = "todaittodait@gmail.com"
            )
            @NotBlank
            @Email
            String email,

            @Schema(
                    description = "회원 비밀번호",
                    example = "todait1234!"
            )

            @NotBlank
            String password
    ) {
        public Login {
            email = MemberInputPolicy.normalizeEmail(email);
        }
    }

    @Schema(name = "AuthLogoutRequest")
    public record Logout(
            @NotBlank
            String refreshToken
    ) {
    }

    @Schema(name = "AuthTokenRefreshRequest")
    public record TokenRefresh(
            @Schema(
                    description = "일반 또는 소셜 로그인 응답에서 발급받은 Refresh Token입니다. "
                            + "실제 테스트 시 발급받은 refreshToken 값으로 교체해야 합니다.",
                    example = "eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlblR5cGUiOiJSRUZSRVNIIiwic3ViIjoiMSJ9.signature"
            )
            @NotBlank
            String refreshToken
    ) {
    }
}

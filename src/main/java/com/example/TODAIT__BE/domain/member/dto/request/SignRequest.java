package com.example.TODAIT__BE.domain.member.dto.request;

import com.example.TODAIT__BE.domain.member.support.MemberInputPolicy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SignRequest {

    public record SignUp(
            @NotBlank
            @Size(min = 2, max = 12)
            @Pattern(
                    regexp = "^[가-힣a-zA-Z0-9]+$",
                    message = "닉네임에는 한글, 영문, 숫자만 사용할 수 있습니다."
            )
            String nickname,

            @NotBlank
            @Email
            String email,

            @NotBlank
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,72}$",
                    message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8자 이상이어야 합니다."
            )
            String password,

            @NotEmpty
            List<@Valid TermAgreementRequest> termAgreements
    ){
        public SignUp {
            nickname = MemberInputPolicy.normalizeNickname(nickname);
            email = MemberInputPolicy.normalizeEmail(email);
        }
    }

}

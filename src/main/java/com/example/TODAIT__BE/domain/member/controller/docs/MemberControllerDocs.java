package com.example.TODAIT__BE.domain.member.controller.docs;

import com.example.TODAIT__BE.domain.member.dto.response.MemberResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(
        name = "MEMBER",
        description = "로그인, 회원가입, 이메일 인증, 온보딩, 회원 정보 API"
)
public interface MemberControllerDocs {

    @Operation(
            summary = "[회원 정보] 닉네임 사용 가능 확인",
            description = """
                    입력한 닉네임의 사용 가능 여부를 확인합니다.

                    이미 사용 중인 닉네임이어도 API 오류로 처리하지 않고,
                    정상 응답으로 `available=false`를 반환합니다.

                    ### 사용 가능한 닉네임인 경우

                    ```json
                    {
                      "isSuccess": true,
                      "code": "MEMBER200_4",
                      "message": "닉네임 중복 확인에 성공했습니다.",
                      "result": {
                        "nickname": "투데잇",
                        "available": true
                      }
                    }
                    ```

                    ### 이미 사용 중인 닉네임인 경우

                    ```json
                    {
                      "isSuccess": true,
                      "code": "MEMBER200_4",
                      "message": "닉네임 중복 확인에 성공했습니다.",
                      "result": {
                        "nickname": "투데잇",
                        "available": false
                      }
                    }
                    ```

                    ### nickname 값이 누락되었거나 형식에 맞지 않는 경우

                    ```json
                    {
                      "isSuccess": false,
                      "code": "COMMON400_1",
                      "message": "잘못된 요청입니다.",
                      "result": null
                    }
                    ```
                    """
    )
    ResponseEntity<ApiResponse<MemberResponse.NicknameAvailability>> checkNicknameAvailability(
            @RequestParam
            @NotBlank(message = "닉네임은 필수입니다.")
            @Size(min = 2, max = 12, message = "닉네임은 2자 이상 12자 이하여야 합니다.")
            @Pattern(
                    regexp = "^[\\uAC00-\\uD7A3a-zA-Z0-9]+$",
                    message = "닉네임에는 한글, 영문, 숫자만 사용할 수 있습니다."
            )
            String nickname
    );

    @Operation(
            summary = "[회원 정보] 내 정보 조회",
            description = """
                    현재 로그인한 사용자의 기본 정보와 마이페이지 요약 정보를 조회합니다.

                    - 기본 정보: 회원 ID, 이메일, 닉네임, 프로필 이미지
                    - 요약 정보: 저장 코스 개수
                    """
    )
    ResponseEntity<ApiResponse<MemberResponse.Me>> getMyInfo(
            @AuthenticationPrincipal AuthMember authMember
    );
}

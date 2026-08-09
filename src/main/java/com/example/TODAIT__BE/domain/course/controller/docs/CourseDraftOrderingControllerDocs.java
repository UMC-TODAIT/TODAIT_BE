package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.OrderingEntryResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "COURSE", description = "임시 코스 및 저장 코스 API")
public interface CourseDraftOrderingControllerDocs {

    @Operation(
            summary = "[순서 설정] 순서 설정 화면 진입",
            description = """
                    장소 선택을 완료하고 드래그 순서 설정 화면에 진입할 때 호출합니다.

                    장소 구성 무결성을 검증한 뒤 상태가 PLACE_SELECTING 이면 ORDERING 으로 전환하고,
                    이미 ORDERING 이면 상태 변경 없이 동일한 성공 응답을 반환합니다(멱등).

                    - 상태 전이: PLACE_SELECTING -> ORDERING
                    - 멱등 처리: ORDERING 상태 재호출 가능
                    - 실제 순서 변경: 선택 장소 순서 변경 API 사용
                    """
    )
    ResponseEntity<ApiResponse<OrderingEntryResponse>> enterOrdering(
            Long courseDraftId,
            AuthMember authMember
    );
}

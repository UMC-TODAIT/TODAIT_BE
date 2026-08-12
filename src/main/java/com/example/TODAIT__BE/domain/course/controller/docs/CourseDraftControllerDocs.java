package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.BasePlaceSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.FoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.MoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceAddRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.StatusUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.AbandonResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.BasePlaceSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CreateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CurrentResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.FoodCategorySaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.MoodTagSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.OrderingEntryResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.PlaceAddResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.PlaceOrderUpdateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.SavingEnterResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.StatusUpdateResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "COURSE",
        description = "임시 코스 및 저장 코스 API"
)
public interface CourseDraftControllerDocs {

    @Operation(
            summary = "[임시 코스 생성] 코스 생성 시작",
            description = """
                    코스 생성 플로우를 시작하기 위한 새로운 임시 코스를 생성합니다.

                    기존 미완료 임시 코스가 존재하더라도 매 요청마다 새로운 Draft를 생성합니다.

                    - 최초 상태: MOOD_SELECTING
                    - 인증: Access Token 필요
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<CreateResponse>>
    createCourseDraft(
            @Parameter(hidden = true)
            AuthMember authMember
    );

    @Operation(
            summary = "[임시 코스 조회] 진행 중인 임시 코스 조회",
            description = """
                    로그인 사용자의 최신 진행 중 임시 코스를 조회합니다.

                    COMPLETED, ABANDONED 상태는 제외하며,
                    진행 중 Draft가 없으면 200 OK와 result=null을 반환합니다.

                    여러 Draft가 진행 중이면 updatedAt DESC, id DESC 기준 최신 1건을 반환합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<CurrentResponse>>
    getCurrentCourseDraft(
            @Parameter(hidden = true)
            AuthMember authMember
    );

    @Operation(
            summary = "[분위기 선택] 분위기 태그 저장",
            description = """
                    임시 코스에 분위기 태그 선택값 전체를 PUT 방식으로 교체 저장합니다.

                    - 선택 개수: 2개 이상 6개 이하
                    - MOOD_SELECTING 상태: 저장 후 FOOD_SELECTING으로 전이
                    - FOOD_SELECTING 상태: 태그만 교체하고 상태 유지
                    - 기존 분위기 태그와 실제 값이 달라지고 저장된 장소가 있으면 장소 선택 데이터를 초기화합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<MoodTagSaveResponse>> saveMoodTags(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember,
            @RequestBody
            MoodTagSaveRequest request
    );

    @Operation(
            summary = "[음식 선택] 음식 카테고리 저장",
            description = """
                    임시 코스에 음식 카테고리 선택값 전체를 PUT 방식으로 교체 저장합니다.

                    - 선택 개수: 1개 이상
                    - FOOD_SELECTING 상태: 저장 후 BASE_PLACE_SELECTING으로 전이
                    - BASE_PLACE_SELECTING 상태: 카테고리만 교체하고 상태 유지
                    - 기존 음식 카테고리와 실제 값이 달라지고 저장된 장소가 있으면 장소 선택 데이터를 초기화합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<FoodCategorySaveResponse>> saveFoodCategories(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember,
            @RequestBody
            FoodCategorySaveRequest request
    );

    @Operation(
            summary = "기준 장소 설정",
            description = "임시 코스의 기준 장소를 저장합니다. 내부 DB에 존재하는 장소는 placeId로, "
                    + "카카오 검색 결과 중 내부 DB에 없는 장소는 externalPlace로 전달합니다. "
                    + "BASE_PLACE_SELECTING 상태에서만 호출 가능하며, 성공 시 PLACE_SELECTING으로 전이합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<BasePlaceSaveResponse>> saveBasePlace(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember,
            @RequestBody
            BasePlaceSaveRequest request
    );

    @Operation(
            summary = "[장소 선택] 선택 장소 추가",
            description = """
                    카테고리별 추천 장소 카드 중 하나를 현재 임시 코스에 선택 장소로 추가합니다.

                    - PLACE_SELECTING 상태에서만 호출 가능하며, 성공 후에도 PLACE_SELECTING을 유지합니다.
                    - 기준 장소와 동일한 장소, 이미 선택한 장소는 추가할 수 없습니다.
                    - 이미 선택된 카테고리(기준 장소 포함)와 같은 카테고리의 장소는 추가할 수 없습니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<PlaceAddResponse>> addPlace(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember,
            @RequestBody
            PlaceAddRequest request
    );

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
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<OrderingEntryResponse>> enterOrdering(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember
    );

    @Operation(
            summary = "[순서 설정] 장소 순서 변경",
            description = """
                    임시 코스에 담긴 전체 장소들의 방문 순서를 일괄 변경합니다.

                    - BASE 포함 전체 장소를 요청에 포함합니다.
                    - 방문 순서는 1번부터 연속되어야 합니다.
                    - 성공 후 1번 장소는 BASE, 나머지는 SELECTED로 재지정합니다.
                    - 성공 후 draftStatus는 ORDERING입니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<PlaceOrderUpdateResponse>> updatePlaceOrder(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember,
            @RequestBody
            PlaceOrderUpdateRequest request
    );

    @Operation(
            summary = "[저장 준비] 저장 화면 진입",
            description = """
                    임시 코스를 ORDERING에서 SAVING 상태로 전환합니다.

                    저장 전 기준 장소와 선택 장소 구성이 유효한지 확인합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<SavingEnterResponse>> enterSaving(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember
    );

    @Operation(
            summary = "[단계 이동] 임시 코스 이전 단계 이동",
            description = """
                    임시 코스를 현재보다 앞선 작성 단계로 되돌립니다.

                    이전 단계 이동은 화면 이동으로 취급하므로 기존 선택 데이터는 삭제하지 않습니다.
                    ORDERING으로 이동할 때만 장소 구성을 검증합니다.
                    COMPLETED 또는 ABANDONED Draft는 되돌릴 수 없습니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<StatusUpdateResponse>> updateStatus(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember,
            @RequestBody
            StatusUpdateRequest request
    );

    @Operation(
            summary = "[임시 코스 포기] 진행 중인 임시 코스 포기",
            description = """
                    작성 중인 임시 코스를 ABANDONED 상태로 전환합니다.

                    DB 행과 하위 mood/food/place 데이터는 즉시 삭제하지 않고,
                    expiresAt은 포기 시점부터 30일 뒤로 설정합니다.
                    COMPLETED 또는 ABANDONED 상태에서는 409를 반환합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<AbandonResponse>> abandonCourseDraft(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember
    );
}

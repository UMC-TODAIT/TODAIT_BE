package com.example.TODAIT__BE.domain.place.controller;

import com.example.TODAIT__BE.domain.place.code.PlaceDetailSuccessCode;
import com.example.TODAIT__BE.domain.place.code.PlaceSearchSuccessCode;
import com.example.TODAIT__BE.domain.place.controller.docs.PlaceControllerDocs;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.service.PlaceSearchService;
import com.example.TODAIT__BE.domain.place.service.PlaceService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceController implements PlaceControllerDocs {

    private final PlaceSearchService placeSearchService;
    private final PlaceService placeService;

    @GetMapping("/search")
    @Override
    public ResponseEntity<ApiResponse<PlaceSearchResponse.SearchResult>> searchPlaces(
            @RequestParam(
                    name = "query",
                    required = false
            )
            String query,
            @RequestParam(
                    name = "cursor",
                    required = false
            )
            Integer cursor,
            @RequestParam(
                    name = "size",
                    required = false
            )
            Integer size
    ) {
        PlaceSearchResponse.SearchResult result =
                placeSearchService.search(query, cursor, size);

        return ResponseEntity
                .status(
                        PlaceSearchSuccessCode
                                .PLACE_SEARCH_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                PlaceSearchSuccessCode.PLACE_SEARCH_OK,
                                result
                        )
                );
    }

    @GetMapping("/{placeId}")
    @Override
    public ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlaceDetail(
            @PathVariable Long placeId
    ) {
        PlaceDetailResponse result = placeService.getPlaceDetail(placeId);
        return ResponseEntity
                .status(PlaceDetailSuccessCode.PLACE_DETAIL_OK.getStatus())
                .body(ApiResponse.onSuccess(PlaceDetailSuccessCode.PLACE_DETAIL_OK, result));
    }
}

package com.example.TODAIT__BE.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.place.code.PlaceSearchErrorCode;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceSearchResult;
import com.example.TODAIT__BE.domain.place.service.port.PlaceSearchPort;
import com.example.TODAIT__BE.domain.place.service.support.PlaceSearchEnricher;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceSearchServiceTest {

    @Mock
    private PlaceSearchPort placeSearchPort;

    @Mock
    private PlaceSearchEnricher searchEnricher;

    @InjectMocks
    private PlaceSearchService placeSearchService;

    @Test
    void returnsNextCursorWhenExternalSearchHasNextPage() {
        ExternalPlaceSearchResult externalResult =
                new ExternalPlaceSearchResult(List.of(), false);

        given(placeSearchPort.searchByKeyword("성수 카페", 1, 10))
                .willReturn(externalResult);
        given(searchEnricher.enrich(List.of())).willReturn(List.of());

        PlaceSearchResponse.SearchResult result =
                placeSearchService.search(" 성수 카페 ", null, null);

        assertThat(result.query()).isEqualTo("성수 카페");
        assertThat(result.nextCursor()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.places()).isEmpty();
        verify(placeSearchPort).searchByKeyword("성수 카페", 1, 10);
    }

    @Test
    void returnsNoNextCursorWhenExternalSearchEnds() {
        ExternalPlaceSearchResult externalResult =
                new ExternalPlaceSearchResult(List.of(), true);

        given(placeSearchPort.searchByKeyword("연남 카페", 3, 15))
                .willReturn(externalResult);
        given(searchEnricher.enrich(List.of())).willReturn(List.of());

        PlaceSearchResponse.SearchResult result =
                placeSearchService.search("연남 카페", 3, 15);

        assertThat(result.nextCursor()).isNull();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void rejectsCursorOutsideKakaoPageRange() {
        assertThatThrownBy(() -> placeSearchService.search("홍대 카페", 46, 10))
                .isInstanceOfSatisfying(
                        PlaceException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(PlaceSearchErrorCode.INVALID_PLACE_SEARCH_CURSOR)
                );
    }

    @Test
    void rejectsSizeOutsideKakaoPageSizeRange() {
        assertThatThrownBy(() -> placeSearchService.search("홍대 카페", 1, 16))
                .isInstanceOfSatisfying(
                        PlaceException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(PlaceSearchErrorCode.INVALID_PLACE_SEARCH_SIZE)
                );
    }
}

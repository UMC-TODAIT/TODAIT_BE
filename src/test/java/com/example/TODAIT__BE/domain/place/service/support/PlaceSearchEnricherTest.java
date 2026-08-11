package com.example.TODAIT__BE.domain.place.service.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.TODAIT__BE.domain.place.code.PlaceSearchErrorCode;
import com.example.TODAIT__BE.domain.place.enums.PlaceDataSourceCode;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceCandidate;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.repository.AreaRepository;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlaceSearchEnricherTest {

    private final AreaRepository areaRepository = mock(AreaRepository.class);
    private final PlaceCategoryRepository placeCategoryRepository =
            mock(PlaceCategoryRepository.class);
    private final PlaceSearchDataLoader dataLoader = mock(PlaceSearchDataLoader.class);
    private final PlaceSearchImageResolver imageResolver = mock(PlaceSearchImageResolver.class);

    private final PlaceSearchEnricher enricher = new PlaceSearchEnricher(
            areaRepository,
            placeCategoryRepository,
            dataLoader,
            imageResolver
    );

    @Test
    void supportedAreaCandidateFailsWhenMappedCategoryIsNotActive() {
        Area area = mock(Area.class);
        given(area.getCode()).willReturn("SEONGSU");
        given(areaRepository.findAllByIsActiveTrueOrderBySortOrderAsc())
                .willReturn(List.of(area));
        given(placeCategoryRepository.findAllByIsActiveTrueOrderBySortOrderAsc())
                .willReturn(List.of());

        ExternalPlaceCandidate candidate = new ExternalPlaceCandidate(
                PlaceDataSourceCode.KAKAO,
                "external-id",
                "성수 소품샵",
                "서울 성동구 성수동",
                "서울 성동구 연무장길",
                BigDecimal.valueOf(37.54),
                BigDecimal.valueOf(127.05),
                null,
                "https://place.map.kakao.com/external-id",
                "SEONGSU",
                "OTHER",
                "소품샵"
        );

        assertThatThrownBy(() -> enricher.enrich(List.of(candidate)))
                .isInstanceOfSatisfying(
                        PlaceException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        PlaceSearchErrorCode
                                                .PLACE_CATEGORY_CONFIGURATION_MISSING
                                )
                );
    }
}

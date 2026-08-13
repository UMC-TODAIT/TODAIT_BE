package com.example.TODAIT__BE.infra.kakao.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceCandidate;
import com.example.TODAIT__BE.domain.place.service.port.ExternalPlaceSearchResult;
import com.example.TODAIT__BE.infra.kakao.local.dto.KakaoKeywordSearchResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoPlaceSearchAdapterTest {

    @Mock
    private KakaoLocalClient kakaoLocalClient;

    @InjectMocks
    private KakaoPlaceSearchAdapter adapter;

    @Test
    void mapsUnsupportedCategoryToOtherInsideSupportedArea() {
        KakaoKeywordSearchResponse.Document document =
                new KakaoKeywordSearchResponse.Document(
                        "1776787563",
                        "오브젝트 서교점",
                        "가정,생활 > 문구,사무용품",
                        "",
                        "",
                        "02-3144-7738",
                        "서울 마포구 서교동 326-2",
                        "서울 마포구 와우산로35길 13",
                        "126.9298096541413",
                        "37.55569597973931",
                        "https://place.map.kakao.com/1776787563"
                );
        KakaoKeywordSearchResponse.Result response =
                new KakaoKeywordSearchResponse.Result(
                        new KakaoKeywordSearchResponse.Meta(1, 1, true),
                        List.of(document)
                );
        given(kakaoLocalClient.searchByKeyword("오브젝트", 1, 10))
                .willReturn(response);

        ExternalPlaceSearchResult result =
                adapter.searchByKeyword("오브젝트", 1, 10);

        assertThat(result.end()).isTrue();
        assertThat(result.candidates()).hasSize(1);
        ExternalPlaceCandidate candidate = result.candidates().get(0);
        assertThat(candidate.areaCode()).isEqualTo("HONGDAE");
        assertThat(candidate.placeCategoryCode()).isEqualTo("OTHER");
        assertThat(candidate.subCategory()).isEqualTo("문구,사무용품");
    }

    @Test
    void keepsUnsupportedAreaAsNullForRegionalFiltering() {
        KakaoKeywordSearchResponse.Document document =
                new KakaoKeywordSearchResponse.Document(
                        "outside-1",
                        "강남 소품샵",
                        "가정,생활 > 문구,사무용품",
                        "",
                        "",
                        "",
                        "서울 강남구 역삼동 1",
                        "서울 강남구 테헤란로 1",
                        "127.0276",
                        "37.4979",
                        "https://place.map.kakao.com/outside-1"
                );
        KakaoKeywordSearchResponse.Result response =
                new KakaoKeywordSearchResponse.Result(
                        new KakaoKeywordSearchResponse.Meta(1, 1, true),
                        List.of(document)
                );
        given(kakaoLocalClient.searchByKeyword("강남 소품샵", 1, 10))
                .willReturn(response);

        ExternalPlaceCandidate candidate = adapter
                .searchByKeyword("강남 소품샵", 1, 10)
                .candidates()
                .get(0);

        assertThat(candidate.areaCode()).isNull();
        assertThat(candidate.placeCategoryCode()).isEqualTo("OTHER");
    }
}

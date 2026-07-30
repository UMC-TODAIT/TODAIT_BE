package com.example.TODAIT__BE.infra.kakao.local.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class KakaoKeywordSearchResponse {

    private KakaoKeywordSearchResponse(){}

    public record Result(
            Meta meta,
            List<Document> documents
    ){}

    public record Meta(
            @JsonProperty("total_count")
            int totalCount,

            @JsonProperty("pageable_count")
            int pageableCount,

            @JsonProperty("is_end")
            boolean end
    ){}

    public record Document(
            String id,

            @JsonProperty("place_name")
            String placeName,

            @JsonProperty("category_name")
            String categoryName,

            @JsonProperty("category_group_code")
            String categoryGroupCode,

            @JsonProperty("category_group_name")
            String categoryGroupName,

            String phone,

            @JsonProperty("address_name")
            String addressName,

            @JsonProperty("road_address_name")
            String roadAddressName,

            String x,
            String y,

            @JsonProperty("place_url")
            String placeUrl
    ){}
}

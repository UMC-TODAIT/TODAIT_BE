package com.example.TODAIT__BE.infra.kakao.local;

import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.infra.kakao.local.dto.KakaoKeywordSearchResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class KakaoLocalClient {

    private static final String BASE_URL = "https://dapi.kakao.com";
    private static final String KAKAO_AUTH_PREFIX = "KakaoAK ";

    private final RestClient restClient;
    private final String restApiKey;

    public KakaoLocalClient(
            @Qualifier("kakaoRequestFactory")
            ClientHttpRequestFactory kakaoRequestFactory,

            @Value("${app.kakao.local.rest-api-key}")
            String restApiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(kakaoRequestFactory)
                .build();

        this.restApiKey = restApiKey;
    }

    public KakaoKeywordSearchResponse.Result searchByKeyword(
            String query,
            int page,
            int size
    ) {
        try {
            KakaoKeywordSearchResponse.Result response =
                    restClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/v2/local/search/keyword.json")
                                    .queryParam("query", query)
                                    .queryParam("page", page)
                                    .queryParam("size", size)
                                    .queryParam("sort", "accuracy")
                                    .build()
                            )
                            .header(
                                    "Authorization",
                                    KAKAO_AUTH_PREFIX + restApiKey
                            )
                            .retrieve()
                            .body(
                                    KakaoKeywordSearchResponse
                                            .Result.class
                            );

            if (response == null) {
                throw new PlaceException(
                        PlaceErrorCode
                                .KAKAO_LOCAL_API_REQUEST_FAILED
                );
            }

            return response;

        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new PlaceException(
                        PlaceErrorCode
                                .KAKAO_LOCAL_API_RATE_LIMIT_EXCEEDED
                );
            }

            throw new PlaceException(
                    PlaceErrorCode
                            .KAKAO_LOCAL_API_REQUEST_FAILED
            );

        } catch (HttpServerErrorException | ResourceAccessException exception) {
            throw new PlaceException(
                    PlaceErrorCode
                            .KAKAO_LOCAL_API_REQUEST_FAILED
            );
        }
    }

}

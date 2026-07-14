package com.example.TODAIT__BE.infra.oauth;


import com.example.TODAIT__BE.infra.oauth.dto.KakaoTokenResponse;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserInfo;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    private String authorizationUri;
    public String getAuthorizationUrl(){
        return UriComponentsBuilder
                .fromUriString(authorizationUri)
                .queryParam("client_id",clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type","code")
                .build()
                .toUriString();
    }


    public KakaoUserInfo getUserInfo(String code){
        String accessToken = requestAccessToken(code);
        return requestUserInfo(accessToken);
    }

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    private final RestClient restClient = RestClient.create();
    private String requestAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        formData.add("client_secret", clientSecret);

        KakaoTokenResponse response = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(KakaoTokenResponse.class);
        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw new IllegalStateException("카카오 토큰 발급에 실패했습니다.");
        }
        return response.accessToken();
    }

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;
    private KakaoUserInfo requestUserInfo(String accessToken) {
        KakaoUserResponse response = restClient.get()
                .uri(userInfoUri)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(KakaoUserResponse.class);
        if (response == null || response.id() == null) {
            throw new IllegalStateException("카카오 사용자 정보 조회에 실패했습니다.");
        }

        String providerUserId = String.valueOf(response.id());

        String email = null;
        if (response.kakaoAccount() != null) {
            email = response.kakaoAccount().email();
        }


        return new KakaoUserInfo(providerUserId, email);
    }
}

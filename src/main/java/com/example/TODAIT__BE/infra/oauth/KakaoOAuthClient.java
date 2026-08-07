package com.example.TODAIT__BE.infra.oauth;


import com.example.TODAIT__BE.domain.member.code.OAuthErrorCode;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserInfo;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOAuthClient {
    private final RestClient restClient;

    public KakaoOAuthClient(@Qualifier("kakaoRequestFactory") ClientHttpRequestFactory kakaoRequestFactory) {
        this.restClient = RestClient.builder()
                .requestFactory(kakaoRequestFactory)
                .build();
    }


    public KakaoUserInfo getUserInfo(String accessToken){
        return requestUserInfo(accessToken);
    }

    private String resolveProfileImageUrl(
            KakaoUserResponse.Profile profile
    ) {
        if (profile == null
                || Boolean.TRUE.equals(profile.isDefaultImage())
                || !StringUtils.hasText(profile.profileImageUrl())) {
            return null;
        }

        return profile.profileImageUrl();
    }

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;
    private KakaoUserInfo requestUserInfo(String accessToken) {
        // accessToken 비어있음
        if(!StringUtils.hasText(accessToken)){
            throw new MemberException(
                    OAuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN
            );

        }
        try{
            KakaoUserResponse response = restClient.get()
                    .uri(userInfoUri)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .body(KakaoUserResponse.class);

            if (response == null || response.id() == null) {
                throw new MemberException(
                        OAuthErrorCode.KAKAO_USER_INFO_REQUEST_FAILED
                );
            }

            String providerUserId = String.valueOf(response.id());

            String email = null;
            String profileImageUrl = null;

            if (response.kakaoAccount() != null) {
                email = response.kakaoAccount().email();
                profileImageUrl = resolveProfileImageUrl(
                        response.kakaoAccount().profile()
                );
            }

            return new KakaoUserInfo(providerUserId, email,profileImageUrl);
        }catch (HttpClientErrorException e){
            int statusCode = e.getStatusCode().value();

            if(statusCode == 400 || statusCode ==401){
                throw new MemberException(
                        OAuthErrorCode.INVALID_KAKAO_ACCESS_TOKEN
                );
            }

            throw new MemberException(
                    OAuthErrorCode.KAKAO_USER_INFO_REQUEST_FAILED
            );
        }catch (HttpServerErrorException e){
            throw new MemberException(
                    OAuthErrorCode.KAKAO_USER_INFO_REQUEST_FAILED
            );
        }catch (ResourceAccessException e){
            throw new MemberException(
                    OAuthErrorCode.KAKAO_USER_INFO_REQUEST_FAILED
            );
        }






    }
}

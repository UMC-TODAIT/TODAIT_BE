package com.example.TODAIT__BE.infra.oauth;

import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserClient;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserInfo;
import com.example.TODAIT__BE.infra.oauth.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoOAuthUserClient implements OAuthUserClient {

    private final KakaoOAuthClient kakaoOAuthClient;

    @Override
    public OAuthProvider supports() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo getUserInfo(String token) {
        KakaoUserInfo userInfo = kakaoOAuthClient.getUserInfo(token);
        return new OAuthUserInfo(userInfo.providerUserId(), userInfo.email(), userInfo.profileImageUrl());
    }
}

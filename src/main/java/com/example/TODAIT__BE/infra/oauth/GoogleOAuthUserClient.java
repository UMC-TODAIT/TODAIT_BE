package com.example.TODAIT__BE.infra.oauth;

import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserClient;
import com.example.TODAIT__BE.domain.member.service.port.OAuthUserInfo;
import com.example.TODAIT__BE.infra.oauth.dto.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleOAuthUserClient implements OAuthUserClient {

    private final GoogleOAuthClient googleOAuthClient;

    @Override
    public OAuthProvider supports() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public OAuthUserInfo getUserInfo(String token) {
        GoogleUserInfo userInfo = googleOAuthClient.verifyIdToken(token);
        return new OAuthUserInfo(userInfo.providerUserId(), userInfo.email(), userInfo.profileImageUrl());
    }
}

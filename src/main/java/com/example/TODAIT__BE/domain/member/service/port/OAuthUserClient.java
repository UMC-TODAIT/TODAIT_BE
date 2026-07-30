package com.example.TODAIT__BE.domain.member.service.port;

import com.example.TODAIT__BE.domain.member.enums.OAuthProvider;

public interface OAuthUserClient {

    OAuthProvider supports();

    OAuthUserInfo getUserInfo(String token);
}

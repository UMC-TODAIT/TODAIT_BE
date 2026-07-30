package com.example.TODAIT__BE.infra.oauth;


import com.example.TODAIT__BE.domain.member.code.OAuthErrorCode;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.infra.oauth.dto.GoogleUserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleOAuthClient {

    private final GoogleIdTokenVerifier verifier;

    public GoogleOAuthClient(
            @Value("${app.oauth.google.web-client-id}")
            String webClientId
    ){
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(webClientId))
                .build();
    }

    public GoogleUserInfo verifyIdToken(String idTokenValue){
        GoogleIdToken idToken;

        try{
            idToken = GoogleIdToken.parse(
                    verifier.getJsonFactory(),
                    idTokenValue
            );
        }catch (IOException | IllegalArgumentException e){
            throw new MemberException(
                    OAuthErrorCode.INVALID_GOOGLE_ID_TOKEN
            );
        }

        try{

            // 잘못된 토큰일 경우
            if(!verifier.verify(idToken)){
                throw new MemberException(
                        OAuthErrorCode.INVALID_GOOGLE_ID_TOKEN
                );
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String providerUserId = payload.getSubject();
            String email = payload.getEmail();

            if(!StringUtils.hasText(providerUserId)
                    || !StringUtils.hasText(email)
                    || !Boolean.TRUE.equals(payload.getEmailVerified())){
                throw new MemberException(
                        OAuthErrorCode.INVALID_GOOGLE_ID_TOKEN
                );
            }

            return new GoogleUserInfo(providerUserId, email);
        //가져오는 과정에서 네트워크 문제
        }catch (IOException e){
            throw new MemberException(
                    OAuthErrorCode.GOOGLE_ID_TOKEN_VERIFICATION_FAILED
            );
        //암호화 서명 검증 문제
        }catch (GeneralSecurityException | IllegalArgumentException e){
            throw new MemberException(
                    OAuthErrorCode.INVALID_GOOGLE_ID_TOKEN
            );
        }
    }

}

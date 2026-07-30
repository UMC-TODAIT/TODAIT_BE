package com.example.TODAIT__BE.domain.member.service;

import com.example.TODAIT__BE.domain.member.dto.request.AuthRequest;
import com.example.TODAIT__BE.domain.member.entity.RefreshToken;
import com.example.TODAIT__BE.domain.member.service.validator.RefreshTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RefreshTokenValidator refreshTokenValidator;

    @Transactional
    public void logout(AuthRequest.Logout request) {
        RefreshToken storedToken = refreshTokenValidator.validateAndGetStoredToken(request.refreshToken());
        storedToken.revoke();
    }
}

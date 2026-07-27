package com.example.TODAIT__BE.global.security;

import com.example.TODAIT__BE.domain.member.enums.MemberRole;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.apiPayload.code.GeneralErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)
                && ACCESS_TOKEN_TYPE.equals(jwtTokenProvider.getTokenType(token))) {
            AuthMember authMember = resolveAuthMember(token);
            if (authMember == null) {
                writeUnauthorizedResponse(response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authMember,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + authMember.role().name()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        return JwtBearerTokenExtractor.extract(authorization)
                .orElse(null);
    }

    private AuthMember resolveAuthMember(String token) {
        try {
            Long memberId = jwtTokenProvider.getMemberId(token);
            MemberRole role = MemberRole.valueOf(jwtTokenProvider.getRole(token));
            return new AuthMember(memberId, role);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        GeneralErrorCode errorCode = GeneralErrorCode.UNAUTHORIZED;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.onFailure(errorCode, null));
    }
}

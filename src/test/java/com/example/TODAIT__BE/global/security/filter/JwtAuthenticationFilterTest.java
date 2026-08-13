package com.example.TODAIT__BE.global.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.global.security.token.JwtTokenProvider;
import com.example.TODAIT__BE.global.security.token.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "token";

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider, new ObjectMapper());
    }

    @Test
    void writesUnauthorizedWhenBearerTokenIsInvalid() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithBearerToken();
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.validateToken(TOKEN)).willReturn(false);

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":\"COMMON401_1\"");
        verify(jwtTokenProvider, never()).getTokenType(TOKEN);
    }

    @Test
    void writesUnauthorizedWhenBearerTokenIsNotAccessToken() throws ServletException, IOException {
        MockHttpServletRequest request = requestWithBearerToken();
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.validateToken(TOKEN)).willReturn(true);
        given(jwtTokenProvider.getTokenType(TOKEN)).willReturn(TokenType.REFRESH);

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"code\":\"COMMON401_1\"");
    }

    @Test
    void skipsAccessTokenAuthenticationForOAuthOnboardingRequest()
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest(
                HttpMethod.PATCH.name(),
                "/api/members/me/onboarding"
        );
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isSameAs(request);
        assertThat(response.getStatus()).isEqualTo(200);
        verify(jwtTokenProvider, never()).validateToken(TOKEN);
    }

    @Test
    void doesNotSkipAccessTokenAuthenticationForOtherPatchRequest()
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest(
                HttpMethod.PATCH.name(),
                "/api/members/me"
        );
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtTokenProvider.validateToken(TOKEN)).willReturn(false);

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        verify(jwtTokenProvider).validateToken(TOKEN);
    }

    private MockHttpServletRequest requestWithBearerToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        return request;
    }
}

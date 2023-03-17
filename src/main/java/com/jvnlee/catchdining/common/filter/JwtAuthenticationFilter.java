package com.jvnlee.catchdining.common.filter;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.user.service.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtService jwtService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Authorization 헤더가 없으면 그대로 doFilter() 호출해서 건너뜀 (회원가입이 안된 경우, 최초 로그인한 경우, 토큰이 모두 만료되어 재로그인한 경우)
        if (req.getHeader(AUTHORIZATION) == null || req.getHeader(AUTHORIZATION).isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        String[] authHeader = req.getHeader(AUTHORIZATION).split(" ");

        // "Bearer [Access Token] [Refresh Token]" 3단 구조인지 체크
        if (authHeader.length != 3) {
            throw new IllegalArgumentException("Authorization header의 형식이 올바르지 않습니다.");
        }

        String scheme = authHeader[0];

        // scheme이 "Bearer"인지 검증
        if (!jwtService.validateScheme(scheme)) {
            throw new IllegalArgumentException("Authorization header의 scheme이 올바르지 않습니다.");
        }

        String accessToken = authHeader[1];

        // Access Token이 유효한지 검증, 유효하면 인증 처리하고 doFilter
        if (jwtService.validateToken(accessToken)) {
            authenticate(accessToken);
            chain.doFilter(request, response);
            return;
        }

        String refreshToken = authHeader[2];

        // Access Token은 유효하지 않은데, Refresh Token은 유효한 경우 Access Token 재발급
        if (jwtService.validateToken(refreshToken)) {
            try {
                // 만료된 Access Token으로부터 Authentication 정보 추출 시도
                Authentication authentication = jwtService.getAuthentication(accessToken);
                String newAccessToken = jwtService.createAccessToken(authentication);
                authenticate(newAccessToken);
                res.setHeader(AUTHORIZATION, "Bearer " + newAccessToken + " " + refreshToken);
            } catch (JwtException e) {
                // 인증 정보 추출에 실패 시, 재로그인해서 Access Token과 Refresh Token 모두 새로 발급 받아야함
                throw new IllegalArgumentException("올바르지 않은 토큰입니다. 인증 정보를 불러올 수 없습니다.");
            }
        }

        chain.doFilter(request, response);
    }

    private void authenticate(String accessToken) {
        SecurityContextHolder
                .getContext()
                .setAuthentication(jwtService.getAuthentication(accessToken));
    }

}

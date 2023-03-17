package com.jvnlee.catchdining.common.filter;

import com.jvnlee.catchdining.common.exception.UserNotFoundException;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import com.jvnlee.catchdining.domain.user.service.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.*;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtService jwtService;

    private final UserRepository userRepository;

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
            Long id = Long.valueOf(jwtService.getClaims(refreshToken).get("id", String.class));
            User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

            String newAccessToken = jwtService.createAccessToken(user.getUsername(), user.getAuthorities());
            authenticate(newAccessToken);

            res.setHeader(AUTHORIZATION, "Bearer " + newAccessToken + " " + refreshToken);
        }

        chain.doFilter(request, response);
    }

    private void authenticate(String accessToken) {
        SecurityContextHolder
                .getContext()
                .setAuthentication(jwtService.getAuthentication(accessToken));
    }

}

package com.jvnlee.catchdining.domain.user.service;

import com.jvnlee.catchdining.common.exception.UserNotFoundException;
import com.jvnlee.catchdining.domain.user.dto.JwtDto;
import com.jvnlee.catchdining.domain.user.dto.UserLoginDto;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.concurrent.TimeUnit.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserLoginService implements UserDetailsService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final RedisTemplate<String, String> redisTemplate;

    public JwtDto login(UserLoginDto userLoginDto) {
        String username = userLoginDto.getUsername();
        String password = userLoginDto.getPassword();
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        // 인증 토큰 생성 (username, password, authorities)
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());

        // AuthenticationManager에게 인증 토큰을 넘겨 인증 진행
        Authentication auth = authenticationManagerBuilder.getObject().authenticate(authToken);

        // 인증에 성공하면 JWT 생성
        String accessToken = jwtService.createAccessToken(auth);
        String refreshToken = jwtService.createRefreshToken();

        // Refresh Token을 Redis에 저장
        redisTemplate.opsForValue().set(
                user.getId().toString(),
                refreshToken,
                jwtService.getRefreshExp(),
                MILLISECONDS
        );

        // 인증을 마친 Authentication 객체를 SecurityContextHolder에 보관
        SecurityContextHolder.getContext().setAuthentication(auth);

        return new JwtDto(accessToken, refreshToken);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByUsername(username)
                .orElseThrow(UserNotFoundException::new);
    }

}

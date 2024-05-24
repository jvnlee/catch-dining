package com.jvnlee.catchdining.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.common.filter.JwtAuthenticationFilter;
import com.jvnlee.catchdining.common.filter.JwtExceptionFilter;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import com.jvnlee.catchdining.domain.user.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.config.http.SessionCreationPolicy.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;

    private final ObjectMapper om;

    private final UserRepository userRepository;

    private final RedisTemplate<String, String> redisTemplate;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable() // HTTP Basic authentication 사용 안함
                .csrf().disable() // CSRF protection 사용 안함
                .sessionManagement().sessionCreationPolicy(STATELESS) // 세션 사용 안함
                .and()
                .authorizeHttpRequests()
                .antMatchers("/users", "/login").permitAll() // 회원가입(POST /users), 로그인(POST /login) URL 접근 허용
                .anyRequest().authenticated() // 그 외에는 모두 로그인 후 접근 허용
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED))
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userRepository, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class) // 모든 AuthentiationFilter 중에 가장 앞에 배치
                .addFilterBefore(new JwtExceptionFilter(om),
                        JwtAuthenticationFilter.class); // JwtAuthenticationFilter에서 예외가 발생하면 처리해줄 필터 배치

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // 기본: BcryptPasswordEncoder 제공
    }

}

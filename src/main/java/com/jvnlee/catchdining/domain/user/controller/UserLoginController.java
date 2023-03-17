package com.jvnlee.catchdining.domain.user.controller;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.user.dto.JwtDto;
import com.jvnlee.catchdining.domain.user.dto.UserLoginDto;
import com.jvnlee.catchdining.domain.user.service.UserLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpHeaders.*;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginService userLoginService;

    @PostMapping
    public Response login(UserLoginDto userLoginDto, HttpServletResponse response) {
        JwtDto jwtDto = userLoginService.login(userLoginDto);
        response.setHeader(AUTHORIZATION, "Bearer " + jwtDto.getAccessToken() + " " + jwtDto.getRefreshToken());
        return new Response("로그인 성공");
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleBadCredentials() {
        return new Response("비밀번호가 올바르지 않습니다.");
    }

}

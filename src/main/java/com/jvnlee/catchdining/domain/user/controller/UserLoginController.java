package com.jvnlee.catchdining.domain.user.controller;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.user.dto.UserLoginDto;
import com.jvnlee.catchdining.domain.user.service.UserLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginService userLoginService;

    @PostMapping
    public Response login(UserLoginDto userLoginDto) {
        userLoginService.login(userLoginDto);
        return new Response("로그인 성공");
    }

}

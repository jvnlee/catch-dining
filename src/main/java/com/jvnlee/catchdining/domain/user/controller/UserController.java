package com.jvnlee.catchdining.domain.user.controller;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchRequestDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchResponseDto;
import com.jvnlee.catchdining.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public Response join(@RequestBody UserDto userDto) {
        userService.join(userDto);
        return new Response("회원 가입 성공");
    }

    @GetMapping
    public Response<UserSearchResponseDto> search(@RequestParam String username) {
        UserSearchResponseDto data = userService.search(new UserSearchRequestDto(username));
        return new Response<>("회원 검색 성공", data);
    }

    @PutMapping("/{userId}")
    public Response update(@PathVariable Long userId, @RequestBody UserDto userDto) {
        userService.update(userId, userDto);
        return new Response("회원 정보 업데이트 성공");
    }

    @DeleteMapping("/{userId}")
    public Response delete(@PathVariable Long userId) {
        userService.delete(userId);
        return new Response("회원 탈퇴 성공");
    }

}

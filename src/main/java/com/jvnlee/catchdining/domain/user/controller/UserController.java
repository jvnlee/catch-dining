package com.jvnlee.catchdining.domain.user.controller;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchDto;
import com.jvnlee.catchdining.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public Response join(UserDto userDto) {
        userService.join(userDto);
        return new Response("회원 가입 성공");
    }

    @GetMapping
    public Response<UserSearchDto> search(@RequestParam String username) {
        UserSearchDto data = userService.search(username);
        return new Response<>("회원 검색 성공", data);
    }

    @PutMapping("/{userId}")
    public Response update(@PathVariable Long userId, UserDto userDto) {
        userService.update(userId, userDto);
        return new Response("회원 정보 업데이트 성공");
    }

    @DeleteMapping("/{userId}")
    public Response delete(@PathVariable Long userId) {
        userService.delete(userId);
        return new Response("회원 탈퇴 성공");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleDuplicateData(DuplicateKeyException e) {
        return new Response(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response handleNoData() {
        return new Response("해당 username을 가진 사용자가 존재하지 않습니다.");
    }

}

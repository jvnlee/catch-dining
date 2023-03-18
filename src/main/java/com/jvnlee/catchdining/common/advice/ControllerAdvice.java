package com.jvnlee.catchdining.common.advice;

import com.jvnlee.catchdining.common.exception.UserNotFoundException;
import com.jvnlee.catchdining.common.web.Response;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(BAD_REQUEST)
    public Response handleUserNotFound() {
        return new Response("존재하지 않는 사용자입니다.");
    }

}
package com.jvnlee.catchdining.common.web;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response<T> {

    private String message;

    private T data;

    public Response(T data) {
        this.data = data;
    }

    public Response(String message) {
        this.message = message;
    }

}

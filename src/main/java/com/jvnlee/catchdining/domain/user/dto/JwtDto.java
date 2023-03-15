package com.jvnlee.catchdining.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtDto {

    private String accessToken;

    private String refreshToken;

}

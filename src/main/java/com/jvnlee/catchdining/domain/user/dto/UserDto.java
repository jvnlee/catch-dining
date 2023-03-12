package com.jvnlee.catchdining.domain.user.dto;

import com.jvnlee.catchdining.domain.user.model.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {

    private String username;

    private String password;

    private String phoneNumber;

    private UserType userType;

    public UserDto(String username, String password, String phoneNumber, UserType userType) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
    }

}

package com.jvnlee.catchdining.domain.user.model;

import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.entity.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;

    private String password;

    private String phoneNumber;

    @Enumerated
    private UserType userType;

    @OneToMany(mappedBy = "user")
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Review> reviews = new ArrayList<>();

    public User(UserDto userDto) {
        this.username = userDto.getUsername();
        this.password = userDto.getPassword();
        this.phoneNumber = userDto.getPhoneNumber();
        this.userType = userDto.getUserType();
    }

    public void update(UserDto userDto) {
        this.username = userDto.getUsername();
        this.password = userDto.getPassword();
        this.phoneNumber = userDto.getPhoneNumber();
    }

}

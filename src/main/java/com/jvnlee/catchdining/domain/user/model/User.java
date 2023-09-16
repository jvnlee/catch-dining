package com.jvnlee.catchdining.domain.user.model;

import com.jvnlee.catchdining.domain.notification.model.NotificationRequest;
import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.entity.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class User extends BaseEntity implements UserDetails {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String fcmToken;

    @OneToMany(mappedBy = "user")
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Reservation> reservations = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<NotificationRequest> notificationRequests = new ArrayList<>();

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

    public void registerFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.userType.toString()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}

package com.jvnlee.catchdining.domain.user.service;

import com.jvnlee.catchdining.common.exception.UserNotFoundException;
import com.jvnlee.catchdining.domain.review.model.Review;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchRequestDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchResponseDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchResultDto;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.model.UserType;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import com.jvnlee.catchdining.undeveloped.Favorite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    class UserSearchResultDtoImpl implements UserSearchResultDto {
        private Long id;
        private String username;
        private List<Favorite> favorites;
        private List<Review> reviews;

        protected UserSearchResultDtoImpl(Long id, String username, List<Favorite> favorites, List<Review> reviews) {
            this.id = id;
            this.username = username;
            this.favorites = favorites;
            this.reviews = reviews;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public List<Favorite> getFavorites() {
            return favorites;
        }

        @Override
        public List<Review> getReviews() {
            return reviews;
        }
    }

    @Test
    @DisplayName("회원 가입 성공")
    void join_success() {
        UserDto userDto = new UserDto("user", "123", "01012345678", UserType.CUSTOMER);

        userService.join(userDto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원 가입 실패: 중복된 username")
    void join_fail_username() {
        UserDto userDto = new UserDto("user", "123", "01012345678", UserType.CUSTOMER);
        User user = new User(userDto);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.join(userDto))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("회원 가입 실패: 중복된 phoneNumber")
    void join_fail_phoneNumber() {
        UserDto userDto = new UserDto("user", "123", "01012345678", UserType.CUSTOMER);
        User user = new User(userDto);

        when(userRepository.findByPhoneNumber("01012345678")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.join(userDto))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("회원 검색 성공")
    void search_success() {
        UserSearchResultDtoImpl userSearchResultDto = new UserSearchResultDtoImpl(1L, "user", Collections.emptyList(), Collections.emptyList());

        when(userRepository.findInfoByUsername(any())).thenReturn(Optional.of(userSearchResultDto));

        UserSearchResponseDto userSearchResponseDto = userService.search(new UserSearchRequestDto("user"));

        verify(userRepository).findInfoByUsername(any());
        assertThat(userSearchResponseDto.getUsername()).isEqualTo("user");
    }

    @Test
    @DisplayName("회원 검색 실패")
    void search_fail() {
        when(userRepository.findInfoByUsername(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.search(new UserSearchRequestDto("user")))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository).findInfoByUsername(any());
    }

    @Test
    @DisplayName("회원 정보 수정 성공")
    void update_success() {
        Long id = 1L;
        UserDto userDto = new UserDto("user", "123", "01012345678", UserType.CUSTOMER);

        User user = mock(User.class);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        userService.update(id, userDto);

        verify(userRepository).findById(id);
        verify(user).update(userDto);
    }

    @Test
    @DisplayName("회원 정보 수정 실패: 중복된 username")
    void update_fail_username() {
        Long id = 1L;
        UserDto userDto = new UserDto("user", "123", "01012345678", UserType.CUSTOMER);
        User spy = spy(new User(userDto));
        Optional<User> user = Optional.of(spy);

        when(userRepository.findByUsername("user")).thenReturn(user);
        when(user.get().getId()).thenReturn(2L);

        assertThatThrownBy(() -> userService.update(id, userDto))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("회원 가입 실패: 중복된 phoneNumber")
    void update_fail_phoneNumber() {
        Long id = 1L;
        UserDto userDto = new UserDto("user", "123", "01012345678", UserType.CUSTOMER);
        User spy = spy(new User(userDto));
        Optional<User> user = Optional.of(spy);

        when(userRepository.findByPhoneNumber("01012345678")).thenReturn(user);
        when(user.get().getId()).thenReturn(2L);

        assertThatThrownBy(() -> userService.update(id, userDto))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("회원 탈퇴")
    void delete() {
        userService.delete(any());
        verify(userRepository).deleteById(any());
    }

}
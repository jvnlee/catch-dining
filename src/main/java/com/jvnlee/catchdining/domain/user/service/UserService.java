package com.jvnlee.catchdining.domain.user.service;

import com.jvnlee.catchdining.common.exception.UserNotFoundException;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchRequestDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchResponseDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchResultDto;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public void join(UserDto userDto) {
        validateUsername(userDto);
        validatePhoneNumber(userDto);

        encodePassword(userDto);

        User newUser = new User(userDto);
        userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public UserSearchResponseDto search(UserSearchRequestDto userSearchRequestDto) {
        UserSearchResultDto userSearchResultDto = userRepository
                .findInfoByUsername(userSearchRequestDto.getUsername())
                .orElseThrow(UserNotFoundException::new);
        return new UserSearchResponseDto(userSearchResultDto);
    }

    public void update(Long id, UserDto userDto) {
        validateUsername(id, userDto);
        validatePhoneNumber(id, userDto);

        encodePassword(userDto);

        User user = userRepository.findById(id).orElseThrow();
        user.update(userDto);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public User getCurrentUser() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    }

    public List<String> getFcmTokens(List<Long> userIdList) {
        return userIdList.stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(UserNotFoundException::new)
                        .getFcmToken())
                .collect(toList());
    }

    private void validateUsername(UserDto userDto) {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 username 입니다.");
        }
    }

    private void validateUsername(Long id, UserDto userDto) {
        Optional<User> user = userRepository.findByUsername(userDto.getUsername());
        if (user.isPresent()) {
            if (user.get().getId().equals(id)) return; // 같은 username을 가진 기존 데이터가 자기 자신인 경우는 패스
            throw new DuplicateKeyException("이미 존재하는 username 입니다.");
        }
    }

    private void validatePhoneNumber(UserDto userDto) {
        if (userRepository.findByPhoneNumber(userDto.getPhoneNumber()).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 연락처입니다.");
        }
    }

    private void validatePhoneNumber(Long id, UserDto userDto) {
        Optional<User> user = userRepository.findByPhoneNumber(userDto.getPhoneNumber());
        if (user.isPresent()) {
            if (user.get().getId().equals(id)) return; // 같은 phoneNumber 가진 기존 데이터가 자기 자신인 경우는 패스
            throw new DuplicateKeyException("이미 존재하는 연락처입니다.");
        }
    }

    private void encodePassword(UserDto userDto) {
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);
    }

}

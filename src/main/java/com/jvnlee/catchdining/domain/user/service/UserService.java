package com.jvnlee.catchdining.domain.user.service;

import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchDto;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void join(UserDto userDto) {
        validateUsername(userDto);
        validatePhoneNumber(userDto);
        User newUser = new User(userDto);
        userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public UserSearchDto search(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return new UserSearchDto(user);
    }

    public void update(Long id, UserDto userDto) {
        validateUsername(userDto);
        validatePhoneNumber(userDto);
        User user = userRepository.findById(id).orElseThrow();
        user.update(userDto);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    private void validateUsername(UserDto userDto) {
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 username 입니다.");
        }
    }

    private void validatePhoneNumber(UserDto userDto) {
        if (userRepository.findByPhoneNumber(userDto.getPhoneNumber()).isPresent()) {
            throw new DuplicateKeyException("이미 존재하는 연락처입니다.");
        }
    }

}

package com.jvnlee.catchdining.domain.user.service;

import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserSearchDto;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void join(UserDto userDto) {
        User newUser = new User(userDto);
        userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public UserSearchDto search(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return new UserSearchDto(user);
    }

    public void update(Long id, UserDto userDto) {
        User user = userRepository.findById(id).orElseThrow();
        user.update(userDto);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

}

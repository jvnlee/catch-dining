package com.jvnlee.catchdining.domain.user.repository;

import com.jvnlee.catchdining.domain.user.dto.UserSearchResultDto;
import com.jvnlee.catchdining.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("select u from User u where u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    Optional<UserSearchResultDto> findInfoByUsername(String username);

}

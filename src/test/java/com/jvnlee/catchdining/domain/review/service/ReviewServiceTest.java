package com.jvnlee.catchdining.domain.review.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import com.jvnlee.catchdining.domain.review.dto.ReviewCreateRequestDto;
import com.jvnlee.catchdining.domain.review.model.Review;
import com.jvnlee.catchdining.domain.review.repository.ReviewRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    RestaurantRepository restaurantRepository;

    @Mock
    UserService userService;

    @InjectMocks
    ReviewService reviewService;

    @Test
    @DisplayName("리뷰 생성 성공")
    void create_success() {
        ReviewCreateRequestDto reviewCreateRequestDto = new ReviewCreateRequestDto(
                1L,
                4.0,
                4.5,
                5.0,
                "Love this place!"
        );

        User user = mock(User.class);
        Restaurant restaurant = mock(Restaurant.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        reviewService.create(reviewCreateRequestDto);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 생성 실패: 존재하지 않는 식당")
    void create_fail_no_restaurant() {
        ReviewCreateRequestDto reviewCreateRequestDto = new ReviewCreateRequestDto(
                1L,
                4.0,
                4.5,
                5.0,
                "Love this place!"
        );

        User user = mock(User.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(restaurantRepository.findById(anyLong())).thenThrow(RestaurantNotFoundException.class);

        assertThatThrownBy(() -> reviewService.create(reviewCreateRequestDto)).isInstanceOf(RestaurantNotFoundException.class);
        verify(reviewRepository, times(0)).save(any(Review.class));
    }

    @Test
    @DisplayName("특정 유저가 작성한 리뷰 조회")
    void viewByUser() {
        Long userId = 1L;
        reviewService.viewByUser(userId);
        verify(reviewRepository).findAllByUserId(userId);
    }

    @Test
    @DisplayName("특정 식당에 등록된 리뷰 조회")
    void viewByRestaurant() {
        Long restaurantId = 1L;
        reviewService.viewByRestaurant(restaurantId);
        verify(reviewRepository).findAllByRestaurantId(restaurantId);
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void delete() {
        Long reviewId = 1L;
        reviewService.delete(reviewId);
        verify(reviewRepository).deleteById(reviewId);
    }

}
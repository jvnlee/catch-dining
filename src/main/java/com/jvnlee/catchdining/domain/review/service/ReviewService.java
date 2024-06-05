package com.jvnlee.catchdining.domain.review.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import com.jvnlee.catchdining.domain.review.dto.ReviewViewByRestaurantResponseDto;
import com.jvnlee.catchdining.domain.review.dto.ReviewViewByUserResponseDto;
import com.jvnlee.catchdining.domain.review.dto.ReviewCreateRequestDto;
import com.jvnlee.catchdining.domain.review.model.Review;
import com.jvnlee.catchdining.domain.review.repository.ReviewRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final RestaurantRepository restaurantRepository;

    private final UserService userService;

    public void create(ReviewCreateRequestDto reviewCreateRequestDto) {
        User user = userService.getCurrentUser();
        Restaurant restaurant = restaurantRepository
                .findById(reviewCreateRequestDto.getRestaurantId())
                .orElseThrow(RestaurantNotFoundException::new);

        Review review = new Review(
                user,
                restaurant,
                reviewCreateRequestDto.getTasteRating(),
                reviewCreateRequestDto.getMoodRating(),
                reviewCreateRequestDto.getServiceRating(),
                reviewCreateRequestDto.getContent()
        );

        reviewRepository.save(review);
    }

    public List<ReviewViewByUserResponseDto> viewByUser(Long userId) {
        return reviewRepository
                .findAllByUserId(userId)
                .stream()
                .map(ReviewViewByUserResponseDto::new)
                .collect(toList());
    }

    public List<ReviewViewByRestaurantResponseDto> viewByRestaurant(Long restaurantId) {
        return reviewRepository
                .findAllByRestaurantId(restaurantId)
                .stream()
                .map(ReviewViewByRestaurantResponseDto::new)
                .collect(toList());
    }

    public void delete(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

}

package com.jvnlee.catchdining.domain.review.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import com.jvnlee.catchdining.domain.review.dto.ReviewViewByRestaurantResponseDto;
import com.jvnlee.catchdining.domain.review.dto.ReviewViewByUserResponseDto;
import com.jvnlee.catchdining.domain.review.dto.ReviewCreateRequestDto;
import com.jvnlee.catchdining.domain.review.event.ReviewCreatedEvent;
import com.jvnlee.catchdining.domain.review.model.Review;
import com.jvnlee.catchdining.domain.review.repository.ReviewRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    private final RabbitTemplate rabbitTemplate;

    public void create(ReviewCreateRequestDto reviewCreateRequestDto) {
        User user = userService.getCurrentUser();
        Long restaurantId = reviewCreateRequestDto.getRestaurantId();
        Restaurant restaurant = restaurantRepository
                .findById(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);

        double tasteRating = reviewCreateRequestDto.getTasteRating();
        double moodRating = reviewCreateRequestDto.getMoodRating();
        double serviceRating = reviewCreateRequestDto.getServiceRating();

        Review review = new Review(
                user,
                restaurant,
                tasteRating,
                moodRating,
                serviceRating,
                reviewCreateRequestDto.getContent()
        );

        reviewRepository.save(review);

        ReviewCreatedEvent reviewCreatedEvent = new ReviewCreatedEvent(
                restaurantId,
                tasteRating,
                moodRating,
                serviceRating
        );

        rabbitTemplate.convertAndSend("reviewEventQueue", reviewCreatedEvent);
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

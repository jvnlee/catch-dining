package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.common.annotation.AggregatedData;
import com.jvnlee.catchdining.domain.restaurant.model.RestaurantReviewStat;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantReviewStatRepository;
import com.jvnlee.catchdining.domain.review.model.Review;
import com.jvnlee.catchdining.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.util.NoSuchElementException;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@RequiredArgsConstructor
public class RestaurantReviewStatService {

    private final RestaurantReviewStatRepository restaurantReviewStatRepository;

    private final ReviewRepository reviewRepository;

    @Retryable(
            value = OptimisticLockException.class,
            backoff = @Backoff(delay = 100L),
            maxAttempts = 10
    )
    @AggregatedData
    @Transactional(propagation = REQUIRES_NEW)
    public void update(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(NoSuchElementException::new);
        double tasteRating = review.getTasteRating();
        double moodRating = review.getMoodRating();
        double serviceRating = review.getServiceRating();

        RestaurantReviewStat restaurantReviewStat = restaurantReviewStatRepository
                .findById(review.getRestaurant().getId())
                .orElseGet(() -> {
                    RestaurantReviewStat r = RestaurantReviewStat.from(review.getRestaurant());
                    restaurantReviewStatRepository.save(r);
                    return r;
                });

        restaurantReviewStat.update(tasteRating, moodRating, serviceRating);
    }

}

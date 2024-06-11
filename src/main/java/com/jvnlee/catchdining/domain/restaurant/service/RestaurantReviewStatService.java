package com.jvnlee.catchdining.domain.restaurant.service;

import com.jvnlee.catchdining.common.annotation.AggregatedData;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.model.RestaurantReviewStat;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantReviewStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
@RequiredArgsConstructor
public class RestaurantReviewStatService {

    private final RestaurantReviewStatRepository restaurantReviewStatRepository;

    @Retryable(
            value = OptimisticLockException.class,
            backoff = @Backoff(delay = 100L),
            maxAttempts = 10
    )
    @AggregatedData
    @Transactional(propagation = REQUIRES_NEW)
    public void update(Restaurant restaurant, double tasteRating, double moodRating, double serviceRating) {
        RestaurantReviewStat restaurantReviewStat = restaurantReviewStatRepository
                .findById(restaurant.getId())
                .orElseGet(() -> {
                    RestaurantReviewStat r = RestaurantReviewStat.from(restaurant);
                    restaurantReviewStatRepository.save(r);
                    return r;
                });

        restaurantReviewStat.update(tasteRating, moodRating, serviceRating);
    }

}

package com.jvnlee.catchdining.domain.review.event;

import com.jvnlee.catchdining.domain.restaurant.service.RestaurantReviewStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReviewCreatedEventHandler {

    private final RestaurantReviewStatService restaurantReviewStatService;

    @Async
    @TransactionalEventListener
    public void handle(ReviewCreatedEvent event) {
        restaurantReviewStatService.update(
                event.getRestaurant(),
                event.getTasteRating(),
                event.getMoodRating(),
                event.getServiceRating()
        );
    }

}

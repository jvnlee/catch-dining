package com.jvnlee.catchdining.domain.restaurant.event;

import com.jvnlee.catchdining.domain.restaurant.service.RestaurantReviewStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RestaurantEventHandler {

    private final RestaurantReviewStatService restaurantReviewStatService;

    @Async
    @TransactionalEventListener
    public void handleCreated(RestaurantCreatedEvent event) {
        restaurantReviewStatService.register(event.getRestaurantId(), event.getRestaurantDto());
    }

    @Async
    @TransactionalEventListener
    public void handleUpdated(RestaurantUpdatedEvent event) {
        restaurantReviewStatService.update(event.getRestaurantId(), event.getRestaurantDto());
    }

    @Async
    @TransactionalEventListener
    public void handleDeleted(RestaurantDeletedEvent event) {
        restaurantReviewStatService.delete(event.getRestaurantId());
    }

}

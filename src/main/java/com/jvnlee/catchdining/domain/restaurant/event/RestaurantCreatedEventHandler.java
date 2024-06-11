package com.jvnlee.catchdining.domain.restaurant.event;

import com.jvnlee.catchdining.domain.restaurant.service.RestaurantReviewStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RestaurantCreatedEventHandler {

    private final RestaurantReviewStatService restaurantReviewStatService;

    @Async
    @TransactionalEventListener
    public void handle(RestaurantCreatedEvent event) {
        restaurantReviewStatService.register(event.getRestaurant());
    }

}

package com.jvnlee.catchdining.domain.restaurant.event;

import com.jvnlee.catchdining.domain.restaurant.service.RestaurantReviewStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RabbitListener(queues = "restaurantEventQueue")
public class RestaurantEventHandler {

    private final RestaurantReviewStatService restaurantReviewStatService;

    @Async
    @RabbitHandler
    public void handleCreated(RestaurantCreatedEvent event) {
        restaurantReviewStatService.register(event.getRestaurantId(), event.getRestaurantDto());
    }

    @Async
    @RabbitHandler
    public void handleUpdated(RestaurantUpdatedEvent event) {
        restaurantReviewStatService.update(event.getRestaurantId(), event.getRestaurantDto());
    }

    @Async
    @RabbitHandler
    public void handleDeleted(RestaurantDeletedEvent event) {
        restaurantReviewStatService.delete(event.getRestaurantId());
    }

}

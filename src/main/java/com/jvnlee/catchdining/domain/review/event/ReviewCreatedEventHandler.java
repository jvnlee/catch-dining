package com.jvnlee.catchdining.domain.review.event;

import com.jvnlee.catchdining.domain.restaurant.service.RestaurantReviewStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewCreatedEventHandler {

    private final RestaurantReviewStatService restaurantReviewStatService;

    @Async
    @RabbitListener(queues = "reviewEventQueue")
    public void handle(ReviewCreatedEvent event) {
        restaurantReviewStatService.updateReviewData(
                event.getRestaurantId(),
                event.getTasteRating(),
                event.getMoodRating(),
                event.getServiceRating()
        );
    }

}

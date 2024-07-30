package com.jvnlee.catchdining.domain.review.event;

import com.jvnlee.catchdining.domain.restaurant.service.RestaurantReviewStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.jvnlee.catchdining.common.config.RabbitMQConfig.REVIEW_EVENT_QUEUE;

@Component
@RequiredArgsConstructor
public class ReviewEventHandler {

    private final RestaurantReviewStatService restaurantReviewStatService;

    @Async
    @RabbitListener(queues = REVIEW_EVENT_QUEUE)
    public void handleCreated(ReviewCreatedEvent event) {
        restaurantReviewStatService.updateReviewData(
                event.getRestaurantId(),
                event.getTasteRating(),
                event.getMoodRating(),
                event.getServiceRating()
        );
    }

}

package com.jvnlee.catchdining.domain.review.event;

import com.jvnlee.catchdining.common.annotation.RabbitManualAck;
import com.jvnlee.catchdining.domain.restaurant.service.RestaurantReviewStatService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.jvnlee.catchdining.common.config.RabbitMQConfig.REVIEW_EVENT_QUEUE;
import static org.springframework.amqp.support.AmqpHeaders.DELIVERY_TAG;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventHandler {

    private final RestaurantReviewStatService restaurantReviewStatService;

    @Async
    @RabbitListener(queues = REVIEW_EVENT_QUEUE)
    @RabbitManualAck
    public void handleCreated(ReviewCreatedEvent event, Channel channel, @Header(DELIVERY_TAG) long tag) throws IOException {
        restaurantReviewStatService.updateReviewData(
                event.getRestaurantId(),
                event.getTasteRating(),
                event.getMoodRating(),
                event.getServiceRating()
        );
    }

}

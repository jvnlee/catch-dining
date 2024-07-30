package com.jvnlee.catchdining.domain.restaurant.event;

import com.jvnlee.catchdining.domain.restaurant.service.RestaurantReviewStatService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.jvnlee.catchdining.common.config.RabbitMQConfig.RESTAURANT_EVENT_QUEUE;
import static org.springframework.amqp.support.AmqpHeaders.DELIVERY_TAG;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = RESTAURANT_EVENT_QUEUE)
public class RestaurantEventHandler {

    private final RestaurantReviewStatService restaurantReviewStatService;

    @Async
    @RabbitHandler
    public void handleCreated(RestaurantCreatedEvent event, Channel channel, @Header(DELIVERY_TAG) long tag) throws IOException {
        try {
            restaurantReviewStatService.register(event.getRestaurantId(), event.getRestaurantDto());

            channel.basicAck(tag, false);
            log.info("이벤트 처리 완료: {}", event);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            log.error("이벤트 처리 실패: {}", event);
        }
    }

    @Async
    @RabbitHandler
    public void handleUpdated(RestaurantUpdatedEvent event, Channel channel, @Header(DELIVERY_TAG) long tag) throws IOException {
        try {
            restaurantReviewStatService.update(event.getRestaurantId(), event.getRestaurantDto());

            channel.basicAck(tag, false);
            log.info("이벤트 처리 완료: {}", event);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            log.error("이벤트 처리 실패: {}", event);
        }

    }

    @Async
    @RabbitHandler
    public void handleDeleted(RestaurantDeletedEvent event, Channel channel, @Header(DELIVERY_TAG) long tag) throws IOException {
        try {
            restaurantReviewStatService.delete(event.getRestaurantId());

            channel.basicAck(tag, false);
            log.info("이벤트 처리 완료: {}", event);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            log.error("이벤트 처리 실패: {}", event);
        }
    }

}

package com.jvnlee.catchdining.domain.reservation.event;

import com.jvnlee.catchdining.common.annotation.RabbitManualAck;
import com.jvnlee.catchdining.common.config.RabbitMQConfig;
import com.jvnlee.catchdining.domain.notification.service.NotificationRequestService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationEventHandler {

    private final NotificationRequestService notificationRequestService;

    @Async
    @RabbitListener(queues = RabbitMQConfig.RESERVATION_EVENT_QUEUE)
    @RabbitManualAck
    public void handleReservationCancelled(ReservationCancelledEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        notificationRequestService.notify(
                event.getRestaurantId(),
                event.getDate(),
                event.getDiningPeriod(),
                event.getMinHeadCount(),
                event.getMaxHeadCount()
        );
    }

}

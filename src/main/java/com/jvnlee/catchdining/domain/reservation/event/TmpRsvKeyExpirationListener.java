package com.jvnlee.catchdining.domain.reservation.event;

import com.jvnlee.catchdining.domain.reservation.service.ReservationService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class TmpRsvKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final RedisTemplate<String, String> redisTemplate;

    public TmpRsvKeyExpirationListener(RedisMessageListenerContainer listenerContainer, RedisTemplate<String, String> redisTemplate) {
        super(listenerContainer);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey.startsWith(ReservationService.TMP_RSV_SEAT_ID_PREFIX)) {
            String seatId = expiredKey.split(":")[3];
            String tmpSeatAvailQtyKey = ReservationService.TMP_SEAT_AVAIL_QTY_PREFIX + seatId;

            if (Boolean.TRUE.equals(redisTemplate.hasKey(tmpSeatAvailQtyKey))) {
                redisTemplate.opsForValue().increment(tmpSeatAvailQtyKey);
            }
        }
    }

}

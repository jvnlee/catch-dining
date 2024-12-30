package com.jvnlee.catchdining.domain.reservation.event;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import static com.jvnlee.catchdining.common.constant.RedisConstants.SEAT_AVAIL_QTY_PREFIX;
import static com.jvnlee.catchdining.common.constant.RedisConstants.TMP_RSV_SEAT_ID_PREFIX;

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

        if (expiredKey.startsWith(TMP_RSV_SEAT_ID_PREFIX)) {
            String seatId = expiredKey.split(":")[2];
            String tmpSeatAvailQtyKey = SEAT_AVAIL_QTY_PREFIX + seatId;

            if (Boolean.TRUE.equals(redisTemplate.hasKey(tmpSeatAvailQtyKey))) {
                redisTemplate.opsForValue().increment(tmpSeatAvailQtyKey);
            }
        }
    }

}

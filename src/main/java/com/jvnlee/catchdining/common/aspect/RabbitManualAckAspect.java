package com.jvnlee.catchdining.common.aspect;

import com.jvnlee.catchdining.common.annotation.RabbitManualAck;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Aspect
@Component
public class RabbitManualAckAspect {

    @Around("@annotation(rabbitManualAck)")
    public void applyManualAck(ProceedingJoinPoint joinPoint, RabbitManualAck rabbitManualAck) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object event = args[0];
        Channel channel = (Channel) args[1];
        long tag = (Long) args[2];

        try {
            joinPoint.proceed();
            channel.basicAck(tag, false);
            log.info("이벤트 처리 완료: {}", event);
        } catch (IOException e) {
            channel.basicNack(tag, false, true);
            log.error("이벤트 처리 실패: {}", event);
        }
    }

}

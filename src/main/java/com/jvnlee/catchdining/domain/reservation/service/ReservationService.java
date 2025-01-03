package com.jvnlee.catchdining.domain.reservation.service;

import com.jvnlee.catchdining.common.config.RabbitMQConfig;
import com.jvnlee.catchdining.common.exception.CacheInitializationException;
import com.jvnlee.catchdining.common.exception.InvalidRedisKeyException;
import com.jvnlee.catchdining.common.exception.NotEnoughSeatException;
import com.jvnlee.catchdining.common.exception.ReservationNotCancellableException;
import com.jvnlee.catchdining.common.exception.ReservationNotFoundException;
import com.jvnlee.catchdining.common.exception.SeatAvailQtyRollbackException;
import com.jvnlee.catchdining.common.exception.SeatNotFoundException;
import com.jvnlee.catchdining.domain.payment.model.Payment;
import com.jvnlee.catchdining.domain.payment.dto.PaymentDto;
import com.jvnlee.catchdining.domain.payment.service.PaymentService;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRestaurantViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationStatusDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationUserViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationResponseDto;
import com.jvnlee.catchdining.domain.reservation.event.ReservationCancelledEvent;
import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import com.jvnlee.catchdining.domain.reservation.repository.ReservationRepository;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import com.jvnlee.catchdining.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static com.jvnlee.catchdining.common.constant.RedisConstants.*;
import static com.jvnlee.catchdining.common.constant.TimeoutConstants.*;
import static com.jvnlee.catchdining.domain.reservation.model.ReservationStatus.*;
import static com.jvnlee.catchdining.domain.notification.model.DiningPeriod.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

    private final SeatRepository seatRepository;

    private final ReservationRepository reservationRepository;

    private final UserService userService;

    private final PaymentService paymentService;

    private final RedisTemplate<String, String> redisTemplate;

    private final RedissonClient redissonClient;

    private final RabbitTemplate rabbitTemplate;

    public TmpReservationResponseDto createTmp(TmpReservationRequestDto tmpReservationRequestDto) {
        Long seatId = tmpReservationRequestDto.getSeatId();
        String seatAvailQtyKey = SEAT_AVAIL_QTY_PREFIX + seatId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(seatAvailQtyKey))) {
            initializeSeatAvailQtyCache(seatId);
        }

        decrementSeatAvailQtyCache(seatAvailQtyKey);

        String tmpRsvId = storeTmpReservation(seatId);

        return new TmpReservationResponseDto(tmpRsvId);
    }

    private void initializeSeatAvailQtyCache(Long seatId) {
        String seatAvailQtyKey = SEAT_AVAIL_QTY_PREFIX + seatId;
        String lockKey = LOCK_SEAT_PREFIX + seatId;

        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(
                lockKey,
                LOCK_VALUE,
                CACHE_INIT_LOCK_TIMEOUT,
                MILLISECONDS
        );

        RTopic topic = redissonClient.getTopic(TOPIC_SEAT_AVAIL_QTY_PREFIX + seatId);

        try {
            if (Boolean.TRUE.equals(lockAcquired)) {
                try {
                    if (Boolean.FALSE.equals(redisTemplate.hasKey(seatAvailQtyKey))) {
                        initialize(seatId, seatAvailQtyKey, topic);
                    }
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } else {
                waitForCacheInitialization(topic, seatAvailQtyKey);
            }
        } catch (InterruptedException e) {
            throw new CacheInitializationException("자리 잔여 수량 Redis 캐시 초기화 대기 중 쓰레드 인터럽션 발생");
        }
    }

    private void initialize(Long seatId, String seatAvailQtyKey, RTopic topic) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(SeatNotFoundException::new);

        redisTemplate.opsForValue().set(
                seatAvailQtyKey,
                String.valueOf(seat.getAvailableQuantity()),
                SEAT_AVAIL_QTY_CACHE_TIMEOUT,
                MILLISECONDS
        );

        topic.publish(SEAT_AVAIL_QTY_INIT_MSG);
    }

    private void waitForCacheInitialization(RTopic topic, String seatAvailQtyKey) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        topic.addListener(String.class, (channel, msg) -> {
            if (SEAT_AVAIL_QTY_INIT_MSG.equals(msg)) {
                latch.countDown();
            }
        });

        boolean cacheInitialized = latch.await(SEAT_AVAIL_QTY_CACHE_WAIT_TIMEOUT, MILLISECONDS);

        if (!cacheInitialized || Boolean.FALSE.equals(redisTemplate.hasKey(seatAvailQtyKey))) {
            throw new CacheInitializationException("자리 잔여 수량 Redis 캐시 초기화 대기 중 타임아웃 발생");
        }
    }

    private void decrementSeatAvailQtyCache(String seatAvailQtyKey) {
        Long result = redisTemplate.opsForValue().decrement(seatAvailQtyKey);

        if (result == null || result < 0) {
            redisTemplate.opsForValue().increment(seatAvailQtyKey);
            throw new NotEnoughSeatException();
        }
    }

    private String storeTmpReservation(Long seatId) {
        String tmpRsvId = seatId + REDIS_KEY_DELIMITER + UUID.randomUUID();

        redisTemplate.opsForValue().set(
                TMP_RSV_SEAT_ID_PREFIX + tmpRsvId,
                String.valueOf(seatId),
                TMP_RSV_TIMEOUT,
                MILLISECONDS
        );

        redisTemplate.expire(
                SEAT_AVAIL_QTY_PREFIX + seatId,
                SEAT_AVAIL_QTY_CACHE_TIMEOUT,
                MILLISECONDS
        );

        return tmpRsvId;
    }

    public void create(ReservationRequestDto reservationRequestDto) {
        Long seatId = validateTmpRsvKey(reservationRequestDto.getTmpRsvId());

        Seat seat = seatRepository.findWithLockById(seatId)
                .orElseThrow(SeatNotFoundException::new);

        decrementSeatAvailQty(seat);

        Payment payment = paymentService.create(
                new PaymentDto(
                        reservationRequestDto.getReserveMenus(),
                        reservationRequestDto.getPaymentType()
                )
        );

        Reservation reservation = new Reservation(
                userService.getCurrentUser(),
                seat.getRestaurant(),
                LocalDateTime.of(seat.getAvailableDate(), seat.getAvailableTime()),
                seat,
                reservationRequestDto.getHeadCount(),
                payment,
                RESERVED
        );

        reservationRepository.save(reservation);
    }

    private Long validateTmpRsvKey(String tmpRsvId) {
        String tmpRsvSeatIdKey = TMP_RSV_SEAT_ID_PREFIX + tmpRsvId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(tmpRsvSeatIdKey))) {
            throw new InvalidRedisKeyException();
        }

        redisTemplate.delete(tmpRsvSeatIdKey);

        String seatId = tmpRsvId.split(REDIS_KEY_DELIMITER)[0];

        return Long.parseLong(seatId);
    }

    private void decrementSeatAvailQty(Seat seat) {
        if (seat.getAvailableQuantity() == 0) {
            throw new NotEnoughSeatException();
        }

        seat.decrementAvailableQuantity();
    }

    @Transactional(readOnly = true)
    public List<ReservationUserViewDto> viewByUser(Long userId, ReservationStatus status) {
        return reservationRepository.findAllByUserIdAndStatus(userId, status)
                .stream()
                .map(ReservationUserViewDto::new)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationRestaurantViewDto> viewByRestaurant(Long restaurantId, ReservationStatus status) {
        return reservationRepository.findAllByRestaurantIdAndStatus(restaurantId, status)
                .stream()
                .map(ReservationRestaurantViewDto::new)
                .collect(toList());
    }

    public void updateStatus(Long reservationId, ReservationStatusDto reservationStatusDto) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);

        reservation.updateStatus(reservationStatusDto.getStatus());
    }

    public void cancelTmp(String tmpRsvId) {
        Long seatId = validateTmpRsvKey(tmpRsvId);

        redisTemplate.opsForValue().increment(SEAT_AVAIL_QTY_PREFIX + seatId);
    }

    public void cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);

        validateCancelCondition(reservation);

        reservation.updateStatus(CANCELED);

        paymentService.cancel(reservation.getPayment().getId());

        Long seatId = reservation.getSeat().getId();
        Seat seat = seatRepository.findWithLockById(seatId)
                .orElseThrow(SeatNotFoundException::new);

        incrementSeatAvailQty(seat);
        incrementSeatAvailQtyCache(SEAT_AVAIL_QTY_PREFIX + seatId);

        publishReservationCancelledEvent(seat);
    }

    private void validateCancelCondition(Reservation reservation) {
        boolean isSameUser = reservation.getUser().getId().equals(userService.getCurrentUser().getId());
        boolean isReservedStatus = reservation.getReservationStatus().equals(RESERVED);
        boolean isReservedDateToday = reservation.getTime().toLocalDate().equals(LocalDate.now());

        if (!isSameUser || !isReservedStatus || isReservedDateToday) {
            throw new ReservationNotCancellableException();
        }
    }

    private static void incrementSeatAvailQty(Seat seat) {
        if (seat.getAvailableQuantity() == seat.getQuantity()) {
            throw new SeatAvailQtyRollbackException();
        }

        seat.incrementAvailableQuantity();
    }

    private void incrementSeatAvailQtyCache(String seatAvailQtyKey) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(seatAvailQtyKey))) {
            redisTemplate.opsForValue().increment(seatAvailQtyKey);
        }
    }

    private void publishReservationCancelledEvent(Seat seat) {
        Long restaurantId = seat.getRestaurant().getId();
        LocalDate availableDate = seat.getAvailableDate();
        int minHeadCount = seat.getMinHeadCount();
        int maxHeadCount = seat.getMaxHeadCount();

        if (seat.getAvailableTime().isBefore(LocalTime.of(16, 1))) {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RESERVATION_EVENT_QUEUE,
                    new ReservationCancelledEvent(restaurantId, availableDate, LUNCH, minHeadCount, maxHeadCount)
            );
        } else {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.RESERVATION_EVENT_QUEUE,
                    new ReservationCancelledEvent(restaurantId, availableDate, DINNER, minHeadCount, maxHeadCount)
            );
        }
    }

}

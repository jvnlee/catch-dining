package com.jvnlee.catchdining.domain.reservation.service;

import com.jvnlee.catchdining.common.exception.InvalidRedisKeyException;
import com.jvnlee.catchdining.common.exception.NotEnoughSeatException;
import com.jvnlee.catchdining.common.exception.ReservationNotFoundException;
import com.jvnlee.catchdining.common.exception.SeatNotFoundException;
import com.jvnlee.catchdining.domain.notification.service.NotificationRequestService;
import com.jvnlee.catchdining.domain.payment.model.Payment;
import com.jvnlee.catchdining.domain.payment.dto.PaymentDto;
import com.jvnlee.catchdining.domain.payment.service.PaymentService;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRestaurantViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationStatusDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationUserViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationCancelRequestDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationResponseDto;
import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import com.jvnlee.catchdining.domain.reservation.repository.ReservationRepository;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

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

    private final NotificationRequestService notificationRequestService;

    private final RedisTemplate<String, String> redisTemplate;

    private final RedissonClient redissonClient;

    private final String TMP_SEAT_AVAIL_QTY_PREFIX = "tmp:seat:avail_qty:";

    private final String TMP_RSV_SEAT_ID_PREFIX = "tmp:rsv:seat_id:";

    private final String LOCK_SEAT_PREFIX = "lock:seat:";

    private final String SEAT_AVAIL_QTY_INIT_MSG = "CACHE_INITIALIZED";

    public TmpReservationResponseDto createTmp(TmpReservationRequestDto tmpReservationRequestDto) {
        Long seatId = tmpReservationRequestDto.getSeatId();
        String tmpSeatAvailQtyKey = TMP_SEAT_AVAIL_QTY_PREFIX + seatId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(tmpSeatAvailQtyKey))) {
            String lockKey = LOCK_SEAT_PREFIX + seatId;

            RLock lock = redissonClient.getLock(lockKey);
            RTopic topic = redissonClient.getTopic("seatAvailQtyTopic:" + seatId);

            try {
                if (lock.tryLock(5000, -1, MILLISECONDS)) {
                    try {
                        if (Boolean.FALSE.equals(redisTemplate.hasKey(tmpSeatAvailQtyKey))) {
                            Seat seat = seatRepository.findById(seatId)
                                    .orElseThrow(SeatNotFoundException::new);

                            redisTemplate.opsForValue().set(
                                    tmpSeatAvailQtyKey,
                                    String.valueOf(seat.getAvailableQuantity()),
                                    1_800_000L,
                                    MILLISECONDS
                            );

                            topic.publish(SEAT_AVAIL_QTY_INIT_MSG);
                        }
                    } finally {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                } else {
                    CountDownLatch latch = new CountDownLatch(1);
                    topic.addListener(String.class, (channel, msg) -> {
                        if (SEAT_AVAIL_QTY_INIT_MSG.equals(msg)) {
                            latch.countDown();
                        }
                    });

                    boolean cacheInitialized = latch.await(5000, MILLISECONDS);
                    if (!cacheInitialized || Boolean.FALSE.equals(redisTemplate.hasKey(tmpSeatAvailQtyKey))) {
                        throw new RuntimeException("자리 잔여 수량 Redis 캐시 초기화 대기 중 타임아웃 발생");
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("자리 잔여 수량 Redis 캐시 초기화 대기 중 쓰레드 인터럽션 발생");
            }
        }

        Long result = redisTemplate.opsForValue().decrement(tmpSeatAvailQtyKey, 1);

        if (result == null || result < 0) {
            redisTemplate.opsForValue().increment(tmpSeatAvailQtyKey, 1);
            throw new NotEnoughSeatException();
        }

        String tmpRsvSeatIdKey = TMP_RSV_SEAT_ID_PREFIX + UUID.randomUUID();

        redisTemplate.opsForValue().set(
                tmpRsvSeatIdKey,
                String.valueOf(seatId),
                300000L,
                MILLISECONDS
        );

        return new TmpReservationResponseDto(tmpRsvSeatIdKey);
    }

    public void create(ReservationRequestDto reservationRequestDto) {
        String tmpRsvSeatIdKey = reservationRequestDto.getTmpReservationKey();

        String seatIdStr = redisTemplate.opsForValue().get(tmpRsvSeatIdKey);

        if (seatIdStr == null) {
            throw new InvalidRedisKeyException();
        }

        Long seatId = Long.parseLong(seatIdStr);

        redisTemplate.delete(tmpRsvSeatIdKey);

        Seat seat = seatRepository.findWithLockById(seatId)
                .orElseThrow(SeatNotFoundException::new);

        if (seat.getAvailableQuantity() == 0) {
            throw new NotEnoughSeatException();
        }

        seat.decrementAvailableQuantity();

        Payment payment = paymentService.create(
                new PaymentDto(
                        reservationRequestDto.getReserveMenus(),
                        reservationRequestDto.getPaymentType()
                )
        );

        User user = userService.getCurrentUser();

        Reservation reservation = new Reservation(
                user,
                seat.getRestaurant(),
                LocalDateTime.of(seat.getAvailableDate(), seat.getAvailableTime()),
                seat,
                reservationRequestDto.getHeadCount(),
                payment,
                RESERVED
        );

        reservationRepository.save(reservation);
    }

    public List<ReservationUserViewDto> viewByUser(Long userId, ReservationStatus status) {
        List<ReservationUserViewDto> reservationList = reservationRepository.findAllByUserIdAndStatus(userId, status)
                .stream()
                .map(ReservationUserViewDto::new)
                .collect(toList());

        if (reservationList.isEmpty()) throw new ReservationNotFoundException();

        return reservationList;
    }

    public List<ReservationRestaurantViewDto> viewByRestaurant(Long restaurantId, ReservationStatus status) {
        List<ReservationRestaurantViewDto> reservationList = reservationRepository.findAllByRestaurantIdAndStatus(restaurantId, status)
                .stream()
                .map(ReservationRestaurantViewDto::new)
                .collect(toList());

        if (reservationList.isEmpty()) throw new ReservationNotFoundException();

        return reservationList;
    }

    public void updateStatus(Long reservationId, ReservationStatusDto reservationStatusDto) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);
        reservation.updateStatus(reservationStatusDto.getStatus());
    }

    public void cancelTmp(TmpReservationCancelRequestDto tmpReservationCancelRequestDto) {
        String tmpRsvSeatIdKey = tmpReservationCancelRequestDto.getTmpReservationKey();

        String seatIdStr = redisTemplate.opsForValue().get(tmpRsvSeatIdKey);

        if (seatIdStr == null) {
            throw new InvalidRedisKeyException();
        }

        Long seatId = Long.parseLong(seatIdStr);

        redisTemplate.opsForValue().increment(TMP_SEAT_AVAIL_QTY_PREFIX + seatId, 1);
        redisTemplate.delete(tmpRsvSeatIdKey);
    }

    public void cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);
        Seat seat = reservation.getSeat();

        // 예약되어 있던 좌석의 잔여 수량 원복
        seat.incrementAvailableQuantity();

        // 결제 취소
        paymentService.cancel(reservation.getPayment().getId());

        // 취소된 예약 좌석의 조건에 맞는 빈자리 알림 신청이 존재한다면 알림 발송 (비동기)
        Long restaurantId = seat.getRestaurant().getId();
        LocalDate availableDate = seat.getAvailableDate();
        int minHeadCount = seat.getMinHeadCount();
        int maxHeadCount = seat.getMaxHeadCount();

        if (seat.getAvailableTime().isBefore(LocalTime.of(16, 1))) {
            notificationRequestService.notify(restaurantId, availableDate, LUNCH, minHeadCount, maxHeadCount);
        } else {
            notificationRequestService.notify(restaurantId, availableDate, DINNER, minHeadCount, maxHeadCount);
        }

        reservation.updateStatus(CANCELED);
    }

}

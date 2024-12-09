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
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationDto;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import com.jvnlee.catchdining.domain.reservation.repository.ReservationRepository;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

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

    private final String TMP_SEAT_AVAIL_QTY_PREFIX = "tmp:seat:avail_qty:";

    private final String TMP_RSV_SEAT_ID_PREFIX = "tmp:rsv:seat_id:";

    public String createTmp(TmpReservationRequestDto tmpReservationRequestDto) {
        Long seatId = tmpReservationRequestDto.getSeatId();
        String tmpSeatAvailQtyKey = TMP_SEAT_AVAIL_QTY_PREFIX + seatId;

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(SeatNotFoundException::new);

        redisTemplate.opsForValue().setIfAbsent(
                tmpSeatAvailQtyKey,
                String.valueOf(seat.getAvailableQuantity()),
                1_800_000L,
                MILLISECONDS
        );

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

        return tmpRsvSeatIdKey;
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

        // Payment 시도
        Payment payment = paymentService.create(
                new PaymentDto(
                        reservationDto.getReserveMenus(),
                        reservationDto.getPaymentType()
                )
        );

        // 예약 주체인 사용자
        User user = userService.getCurrentUser();

        // Reservation 객체 생성 및 영속화
        Reservation reservation = new Reservation(
                user,
                seat.getRestaurant(),
                LocalDateTime.of(seat.getAvailableDate(), seat.getAvailableTime()),
                seat,
                reservationDto.getHeadCount(),
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

    public void cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(ReservationNotFoundException::new);
        Seat seat = reservation.getSeat();

        // 예약되어 있던 좌석의 잔여 수량 원복
        seat.release();

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

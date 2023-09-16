package com.jvnlee.catchdining.domain.reservation.service;

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
import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationDto;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import com.jvnlee.catchdining.domain.reservation.repository.ReservationRepository;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.jvnlee.catchdining.domain.reservation.model.ReservationStatus.*;
import static com.jvnlee.catchdining.domain.notification.model.DiningPeriod.*;
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

    @Transactional(timeout = 500)
    public void create(ReservationDto reservationDto) {
        // 예약 가능 여부 확인
        Seat seat = seatRepository
                .findWithLockById(reservationDto.getSeatId())
                .orElseThrow(SeatNotFoundException::new);

        if (seat.getAvailableQuantity() > 0) {
            seat.occupy();
        } else {
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

        seat.release();

        Long restaurantId = seat.getRestaurant().getId();
        LocalDate availableDate = seat.getAvailableDate();
        int minHeadCount = seat.getMinHeadCount();
        int maxHeadCount = seat.getMaxHeadCount();

        if (seat.getAvailableTime().isBefore(LocalTime.of(16, 1))) {
            notificationRequestService.notify(restaurantId, availableDate, LUNCH, minHeadCount, maxHeadCount);
        } else {
            notificationRequestService.notify(restaurantId, availableDate, DINNER, minHeadCount, maxHeadCount);
        }

        paymentService.cancel(reservation.getPayment().getId());

        reservation.updateStatus(CANCELED);
    }

}

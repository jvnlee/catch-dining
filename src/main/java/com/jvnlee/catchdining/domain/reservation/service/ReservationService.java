package com.jvnlee.catchdining.domain.reservation.service;

import com.jvnlee.catchdining.common.exception.NotEnoughSeatException;
import com.jvnlee.catchdining.common.exception.SeatNotFoundException;
import com.jvnlee.catchdining.common.exception.UserNotFoundException;
import com.jvnlee.catchdining.domain.payment.domain.Payment;
import com.jvnlee.catchdining.domain.payment.dto.PaymentDto;
import com.jvnlee.catchdining.domain.payment.service.PaymentService;
import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationDto;
import com.jvnlee.catchdining.domain.reservation.repository.ReservationRepository;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.jvnlee.catchdining.domain.reservation.model.ReservationStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

    private final SeatRepository seatRepository;

    private final UserRepository userRepository;

    private final ReservationRepository reservationRepository;

    private final PaymentService paymentService;

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
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

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
}

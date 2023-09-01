package com.jvnlee.catchdining.domain.reservation.service;

import com.jvnlee.catchdining.common.exception.NotEnoughSeatException;
import com.jvnlee.catchdining.common.exception.PaymentNotFoundException;
import com.jvnlee.catchdining.common.exception.ReservationNotFoundException;
import com.jvnlee.catchdining.domain.payment.dto.ReserveMenuDto;
import com.jvnlee.catchdining.domain.payment.model.Payment;
import com.jvnlee.catchdining.domain.payment.service.PaymentService;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRestaurantViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationStatusDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationUserViewDto;
import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import com.jvnlee.catchdining.domain.reservation.repository.ReservationRepository;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.jvnlee.catchdining.domain.payment.model.PaymentType.*;
import static com.jvnlee.catchdining.domain.reservation.model.ReservationStatus.*;
import static com.jvnlee.catchdining.domain.user.model.UserType.CUSTOMER;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    SeatRepository seatRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    PaymentService paymentService;

    @InjectMocks
    ReservationService reservationService;

    @Test
    @DisplayName("예약 성공")
    void create_success() {
        ReservationDto reservationDto = new ReservationDto(
                1L,
                List.of(new ReserveMenuDto(1L, 8000, 1)),
                CREDIT_CARD,
                2
        );

        Seat seat = mock(Seat.class);
        User user = mock(User.class);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(null, null));

        when(seatRepository.findWithLockById(anyLong())).thenReturn(Optional.of(seat));
        when(seat.getAvailableQuantity()).thenReturn(999);
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        when(seat.getAvailableDate()).thenReturn(LocalDate.of(2023, 1, 1));
        when(seat.getAvailableTime()).thenReturn(LocalTime.of(13, 0, 0));

        reservationService.create(reservationDto);

        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 실패: 자리 부족")
    void create_fail() {
        ReservationDto reservationDto = new ReservationDto(
                1L,
                List.of(new ReserveMenuDto(1L, 8000, 1)),
                CREDIT_CARD,
                2
        );

        Seat seat = mock(Seat.class);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(null, null));

        when(seatRepository.findWithLockById(anyLong())).thenReturn(Optional.of(seat));
        when(seat.getAvailableQuantity()).thenReturn(0);

        assertThatThrownBy(() -> reservationService.create(reservationDto))
                .isInstanceOf(NotEnoughSeatException.class);
    }

    @Test
    @DisplayName("유저가 자신의 예약 내역 조회 성공")
    void viewByUser_success() {
        Reservation reservation = new Reservation(
                mock(User.class),
                Restaurant.builder().name("restaurant").build(),
                LocalDateTime.of(2023, 1, 1, 13, 0, 0),
                mock(Seat.class),
                2,
                mock(Payment.class),
                RESERVED
        );

        when(reservationRepository.findAllByUserIdAndStatus(anyLong(), any(ReservationStatus.class)))
                .thenReturn(List.of(reservation));

        List<ReservationUserViewDto> result = reservationService.viewByUser(1L, RESERVED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRestaurantName()).isEqualTo("restaurant");
    }

    @Test
    @DisplayName("유저가 자신의 예약 내역 조회 실패")
    void viewByUser_fail() {
        when(reservationRepository.findAllByUserIdAndStatus(anyLong(), any(ReservationStatus.class)))
                .thenReturn(emptyList());

        assertThatThrownBy(() -> reservationService.viewByUser(1L, RESERVED))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("식당에 예약된 내역 조회 성공")
    void viewByRestaurant_success() {
        Reservation reservation = new Reservation(
                new User(new UserDto("user", "1234", "01012345678", CUSTOMER)),
                mock(Restaurant.class),
                LocalDateTime.of(2023, 1, 1, 13, 0, 0),
                mock(Seat.class),
                2,
                mock(Payment.class),
                RESERVED
        );

        when(reservationRepository.findAllByRestaurantIdAndStatus(anyLong(), any(ReservationStatus.class)))
                .thenReturn(List.of(reservation));

        List<ReservationRestaurantViewDto> result = reservationService.viewByRestaurant(1L, RESERVED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("user");
    }

    @Test
    @DisplayName("식당에 예약된 내역 조회 실패")
    void viewByRestaurant_fail() {
        when(reservationRepository.findAllByRestaurantIdAndStatus(anyLong(), any(ReservationStatus.class)))
                .thenReturn(emptyList());

        assertThatThrownBy(() -> reservationService.viewByRestaurant(1L, RESERVED))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("예약 상태 업데이트 성공")
    void updateStatus() {
        Reservation reservation = mock(Reservation.class);
        ReservationStatusDto reservationStatusDto = mock(ReservationStatusDto.class);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(reservation));

        reservationService.updateStatus(1L, reservationStatusDto);

        verify(reservation).updateStatus(any(ReservationStatus.class));
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancel_success() {
        Reservation reservation = mock(Reservation.class);
        Seat seat = mock(Seat.class);
        Payment payment = mock(Payment.class);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(reservation));
        when(reservation.getSeat()).thenReturn(seat);
        when(reservation.getPayment()).thenReturn(payment);
        when(payment.getId()).thenReturn(anyLong());

        reservationService.cancel(1L);

        verify(paymentService).cancel(anyLong());
        verify(reservation).updateStatus(CANCELED);
    }

    @Test
    @DisplayName("예약 취소 실패")
    void cancel_fail() {
        Reservation reservation = mock(Reservation.class);
        Seat seat = mock(Seat.class);
        Payment payment = mock(Payment.class);

        when(reservationRepository.findById(anyLong())).thenReturn(Optional.of(reservation));
        when(reservation.getSeat()).thenReturn(seat);
        when(reservation.getPayment()).thenReturn(payment);
        doThrow(new PaymentNotFoundException()).when(paymentService).cancel(anyLong());

        assertThatThrownBy(() -> reservationService.cancel(1L))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(reservation, times(0)).updateStatus(CANCELED);
    }
}
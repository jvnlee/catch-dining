package com.jvnlee.catchdining.domain.reservation.service;

import com.jvnlee.catchdining.common.exception.InvalidRedisKeyException;
import com.jvnlee.catchdining.common.exception.NotEnoughSeatException;
import com.jvnlee.catchdining.common.exception.ReservationNotCancellableException;
import com.jvnlee.catchdining.common.exception.ReservationNotFoundException;
import com.jvnlee.catchdining.domain.payment.dto.PaymentDto;
import com.jvnlee.catchdining.domain.payment.dto.ReserveMenuDto;
import com.jvnlee.catchdining.domain.payment.model.Payment;
import com.jvnlee.catchdining.domain.payment.service.PaymentService;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRestaurantViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationStatusDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationUserViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationResponseDto;
import com.jvnlee.catchdining.domain.reservation.event.ReservationCancelledEvent;
import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import com.jvnlee.catchdining.domain.reservation.repository.ReservationRepository;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.jvnlee.catchdining.common.constant.RedisConstants.LOCK_SEAT_PREFIX;
import static com.jvnlee.catchdining.common.constant.RedisConstants.SEAT_AVAIL_QTY_PREFIX;
import static com.jvnlee.catchdining.common.constant.RedisConstants.TMP_RSV_SEAT_ID_PREFIX;
import static com.jvnlee.catchdining.common.constant.RedisConstants.TOPIC_SEAT_AVAIL_QTY_PREFIX;
import static com.jvnlee.catchdining.domain.payment.model.PaymentType.*;
import static com.jvnlee.catchdining.domain.reservation.model.ReservationStatus.*;
import static com.jvnlee.catchdining.domain.reservation.service.ReservationService.*;
import static com.jvnlee.catchdining.domain.user.model.UserType.CUSTOMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    SeatRepository seatRepository;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    UserService userService;

    @Mock
    PaymentService paymentService;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    RedissonClient redissonClient;

    @Mock
    RabbitTemplate rabbitTemplate;

    @InjectMocks
    ReservationService reservationService;

    @Test
    @DisplayName("임시 예약 성공: 자리 잔여 수량 캐시 히트")
    void create_tmp_success_cache_hit() {
        Long seatId = 1L;
        String tmpSeatAvailQtyKey = SEAT_AVAIL_QTY_PREFIX + seatId;
        TmpReservationRequestDto requestDto = new TmpReservationRequestDto(seatId);

        when(redisTemplate.hasKey(tmpSeatAvailQtyKey)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.decrement(tmpSeatAvailQtyKey, 1L)).thenReturn(9L);

        TmpReservationResponseDto result = reservationService.createTmp(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getTmpRsvId()).isNotBlank();
        verify(redisTemplate).hasKey(tmpSeatAvailQtyKey);
        verify(valueOperations).decrement(tmpSeatAvailQtyKey, 1L);
        verify(valueOperations).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("임시 예약 성공: 자리 잔여 수량 캐시 미스")
    void create_tmp_success_cache_miss() {
        Long seatId = 1L;
        String tmpSeatAvailQtyKey = SEAT_AVAIL_QTY_PREFIX + seatId;
        String lockKey = LOCK_SEAT_PREFIX + seatId;
        String lockValue = "locked";
        long lockTimeout = 5000L;
        RTopic rTopic = mock(RTopic.class);
        Seat seat = mock(Seat.class);
        TmpReservationRequestDto requestDto = new TmpReservationRequestDto(seatId);

        when(redisTemplate.hasKey(tmpSeatAvailQtyKey)).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.setIfAbsent(lockKey, lockValue, lockTimeout, TimeUnit.MILLISECONDS)).thenReturn(true);
        when(redissonClient.getTopic(TOPIC_SEAT_AVAIL_QTY_PREFIX + seatId)).thenReturn(rTopic);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(valueOperations.decrement(tmpSeatAvailQtyKey, 1L)).thenReturn(9L);

        TmpReservationResponseDto result = reservationService.createTmp(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getTmpRsvId()).isNotBlank();
        verify(redisTemplate, times(2)).hasKey(tmpSeatAvailQtyKey);
        verify(valueOperations).setIfAbsent(lockKey, lockValue, lockTimeout, TimeUnit.MILLISECONDS);
        verify(valueOperations, times(2)).set(anyString(), anyString(), anyLong(), any());
        verify(redisTemplate).delete(lockKey);
        verify(valueOperations).decrement(tmpSeatAvailQtyKey, 1L);
    }

    @Test
    @DisplayName("예약 성공")
    void create_success() {
        Long seatId = 1L;
        String tmpRsvId = "1234";
        String tmpRsvSeatIdKey = TMP_RSV_SEAT_ID_PREFIX + tmpRsvId;
        Seat seat = mock(Seat.class);
        User user = mock(User.class);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(
                tmpRsvId,
                List.of(new ReserveMenuDto("Sushi", 8000, 1)),
                CREDIT_CARD,
                2
        );
        PaymentDto paymentDto = new PaymentDto(reservationRequestDto.getReserveMenus(), reservationRequestDto.getPaymentType());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tmpRsvSeatIdKey)).thenReturn(String.valueOf(seatId));
        when(seatRepository.findWithLockById(seatId)).thenReturn(Optional.of(seat));
        when(seat.getAvailableQuantity()).thenReturn(10);
        when(userService.getCurrentUser()).thenReturn(user);
        when(seat.getAvailableDate()).thenReturn(LocalDate.of(2023, 1, 1));
        when(seat.getAvailableTime()).thenReturn(LocalTime.of(13, 0, 0));

        reservationService.create(reservationRequestDto);

        verify(valueOperations).get(tmpRsvSeatIdKey);
        verify(redisTemplate).delete(tmpRsvSeatIdKey);
        verify(seatRepository).findWithLockById(seatId);
        verify(seat).decrementAvailableQuantity();
        verify(paymentService).create(paymentDto);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 실패: 유효하지 않은 임시 예약 키")
    void create_fail_invalid_tmp_rsv_key() {
        String tmpRsvId = "1234";
        String tmpRsvSeatIdKey = TMP_RSV_SEAT_ID_PREFIX + tmpRsvId;
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(
                tmpRsvId,
                List.of(new ReserveMenuDto("Sushi", 8000, 1)),
                CREDIT_CARD,
                2
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tmpRsvSeatIdKey)).thenReturn(null);

        assertThatThrownBy(() -> reservationService.create(reservationRequestDto))
                .isInstanceOf(InvalidRedisKeyException.class);
        verify(valueOperations).get(tmpRsvSeatIdKey);
    }

    @Test
    @DisplayName("예약 실패: 자리 부족")
    void create_fail_not_enough_seat() {
        Long seatId = 1L;
        String tmpRsvId = "1234";
        String tmpRsvSeatIdKey = TMP_RSV_SEAT_ID_PREFIX + tmpRsvId;
        Seat seat = mock(Seat.class);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(
                tmpRsvId,
                List.of(new ReserveMenuDto("Sushi", 8000, 1)),
                CREDIT_CARD,
                2
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tmpRsvSeatIdKey)).thenReturn(String.valueOf(seatId));
        when(seatRepository.findWithLockById(anyLong())).thenReturn(Optional.of(seat));
        when(seat.getAvailableQuantity()).thenReturn(0);

        assertThatThrownBy(() -> reservationService.create(reservationRequestDto))
                .isInstanceOf(NotEnoughSeatException.class);
        verify(valueOperations).get(tmpRsvSeatIdKey);
        verify(redisTemplate).delete(tmpRsvSeatIdKey);
        verify(seatRepository).findWithLockById(seatId);
    }

    @Test
    @DisplayName("유저가 자신의 예약 내역 조회 성공")
    void viewByUser_success() {
        Long userId = 1L;
        String restaurantName = "restaurant";
        ReservationStatus reservationStatus = RESERVED;
        Reservation reservation = new Reservation(
                mock(User.class),
                Restaurant.builder().name(restaurantName).build(),
                LocalDateTime.of(2023, 1, 1, 13, 0, 0),
                mock(Seat.class),
                2,
                mock(Payment.class),
                reservationStatus
        );

        when(reservationRepository.findAllByUserIdAndStatus(userId, reservationStatus))
                .thenReturn(List.of(reservation));

        List<ReservationUserViewDto> result = reservationService.viewByUser(userId, reservationStatus);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRestaurantName()).isEqualTo(restaurantName);
        verify(reservationRepository).findAllByUserIdAndStatus(userId, reservationStatus);
    }

    @Test
    @DisplayName("식당에 예약된 내역 조회 성공")
    void viewByRestaurant_success() {
        Long restaurantId = 1L;
        String username = "user";
        ReservationStatus reservationStatus = RESERVED;
        Reservation reservation = new Reservation(
                new User(new UserDto(username, "1234", "01012345678", CUSTOMER)),
                mock(Restaurant.class),
                LocalDateTime.of(2023, 1, 1, 13, 0, 0),
                mock(Seat.class),
                2,
                mock(Payment.class),
                reservationStatus
        );

        when(reservationRepository.findAllByRestaurantIdAndStatus(restaurantId, reservationStatus))
                .thenReturn(List.of(reservation));

        List<ReservationRestaurantViewDto> result = reservationService.viewByRestaurant(restaurantId, reservationStatus);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo(username);
        verify(reservationRepository).findAllByRestaurantIdAndStatus(restaurantId, reservationStatus);
    }

    @Test
    @DisplayName("예약 상태 업데이트 성공")
    void updateStatus_success() {
        Long reservationId = 1L;
        ReservationStatus reservationStatus = NO_SHOW;
        Reservation reservation = mock(Reservation.class);
        ReservationStatusDto reservationStatusDto = new ReservationStatusDto(reservationStatus);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.updateStatus(reservationId, reservationStatusDto);

        verify(reservation).updateStatus(reservationStatus);
    }

    @Test
    @DisplayName("예약 상태 업데이트 실패: 존재하지 않는 예약")
    void updateStatus_fail_reservation_not_found() {
        Long reservationId = 1L;
        ReservationStatusDto reservationStatusDto = new ReservationStatusDto(NO_SHOW);

        when(reservationRepository.findById(reservationId)).thenThrow(ReservationNotFoundException.class);

        assertThatThrownBy(() -> reservationService.updateStatus(reservationId, reservationStatusDto))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    @DisplayName("임시 예약 취소 성공")
    void cancelTmp_success() {
        String tmpRsvId = "uuid";
        String tmpRsvSeatIdKey = TMP_RSV_SEAT_ID_PREFIX + tmpRsvId;
        Long seatId = 1L;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(tmpRsvSeatIdKey)).thenReturn(String.valueOf(seatId));

        reservationService.cancelTmp(tmpRsvId);

        verify(valueOperations).get(tmpRsvSeatIdKey);
        verify(redisTemplate).delete(tmpRsvSeatIdKey);
        verify(valueOperations).increment(SEAT_AVAIL_QTY_PREFIX + seatId, 1L);
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancel_success() {
        Long reservationId, userId, seatId, paymentId, restaurantId;
        reservationId = userId = seatId = paymentId = restaurantId = 1L;
        Reservation reservation = mock(Reservation.class);
        User user = mock(User.class);
        Seat seat = mock(Seat.class);
        Payment payment = mock(Payment.class);
        Restaurant restaurant = mock(Restaurant.class);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userService.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getReservationStatus()).thenReturn(RESERVED);
        when(reservation.getTime()).thenReturn(LocalDateTime.of(2023, 1, 1, 13, 0));
        when(reservation.getPayment()).thenReturn(payment);
        when(payment.getId()).thenReturn(paymentId);
        when(reservation.getSeat()).thenReturn(seat);
        when(seat.getId()).thenReturn(seatId);
        when(seatRepository.findWithLockById(seatId)).thenReturn(Optional.of(seat));
        when(seat.getAvailableQuantity()).thenReturn(1);
        when(seat.getQuantity()).thenReturn(10);
        when(seat.getRestaurant()).thenReturn(restaurant);
        when(restaurant.getId()).thenReturn(restaurantId);
        when(seat.getAvailableTime()).thenReturn(LocalTime.of(13, 0));

        reservationService.cancel(reservationId);

        verify(reservationRepository).findById(reservationId);
        verify(reservation).updateStatus(ReservationStatus.CANCELED);
        verify(paymentService).cancel(paymentId);
        verify(seatRepository).findWithLockById(seatId);
        verify(seat).incrementAvailableQuantity();
        verify(redisTemplate).hasKey(SEAT_AVAIL_QTY_PREFIX + seatId);
        verify(rabbitTemplate).convertAndSend(anyString(), any(ReservationCancelledEvent.class));
    }

    @Test
    @DisplayName("예약 취소 실패: 예약 상태가 아님")
    void cancel_fail_not_reserved_status() {
        Long reservationId = 1L;
        Long userId = 1L;
        Reservation reservation = mock(Reservation.class);
        User user = mock(User.class);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userService.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getReservationStatus()).thenReturn(VISITED);
        when(reservation.getTime()).thenReturn(LocalDateTime.of(2023, 1, 1, 13, 0));

        assertThatThrownBy(() -> reservationService.cancel(reservationId))
                .isInstanceOf(ReservationNotCancellableException.class);
        verify(reservationRepository).findById(reservationId);
    }

    @Test
    @DisplayName("예약 취소 실패: 당일 취소 시도")
    void cancel_fail_same_day_cancellation() {
        Long reservationId = 1L;
        Long userId = 1L;
        Reservation reservation = mock(Reservation.class);
        User user = mock(User.class);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userService.getCurrentUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        when(reservation.getUser()).thenReturn(user);
        when(reservation.getReservationStatus()).thenReturn(RESERVED);
        when(reservation.getTime()).thenReturn(LocalDateTime.now());

        assertThatThrownBy(() -> reservationService.cancel(reservationId))
                .isInstanceOf(ReservationNotCancellableException.class);
        verify(reservationRepository).findById(reservationId);
    }
}
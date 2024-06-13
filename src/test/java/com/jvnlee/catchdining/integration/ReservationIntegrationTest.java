package com.jvnlee.catchdining.integration;

import com.jvnlee.catchdining.domain.menu.dto.MenuDto;
import com.jvnlee.catchdining.domain.menu.service.MenuService;
import com.jvnlee.catchdining.domain.payment.model.PaymentType;
import com.jvnlee.catchdining.domain.payment.dto.ReserveMenuDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationDto;
import com.jvnlee.catchdining.domain.reservation.repository.ReservationRepository;
import com.jvnlee.catchdining.domain.reservation.service.ReservationService;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.service.RestaurantService;
import com.jvnlee.catchdining.domain.seat.dto.SeatDto;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.model.SeatType;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import com.jvnlee.catchdining.domain.seat.service.SeatService;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.model.UserType;
import com.jvnlee.catchdining.domain.user.service.UserService;
import com.jvnlee.catchdining.util.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class ReservationIntegrationTest extends TestcontainersContext {

    @Autowired
    UserService userService;

    @Autowired
    RestaurantService restaurantService;

    @Autowired
    SeatService seatService;

    @Autowired
    MenuService menuService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Test
    @DisplayName("100개의 예약 요청으로 동시성 테스트")
    void create() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            userService.join(new UserDto("user" + i, "1234", i + "", UserType.CUSTOMER));
        }

        restaurantService.register(RestaurantDto.builder().name("r1").build());

        seatService.add(1L, new SeatDto(
                SeatType.BAR,
                List.of(LocalTime.of(13, 0, 0)),
                1,
                2,
                100
        ));

        menuService.add(1L, List.of(new MenuDto("sushi", 10000)));

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("user" + finalI, "1234", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                );

                try {
                    reservationService.create(new ReservationDto(
                            1L,
                            List.of(new ReserveMenuDto(1L, 8000, 1)),
                            PaymentType.CREDIT_CARD,
                            2
                    ));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Seat seat = seatRepository.findById(1L).orElseThrow();
        assertThat(seat.getAvailableQuantity()).isEqualTo(0);
        assertThat(reservationRepository.findAll().size()).isEqualTo(100);
    }
}
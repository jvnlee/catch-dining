package com.jvnlee.catchdining.integration;

import com.jvnlee.catchdining.domain.menu.dto.MenuDto;
import com.jvnlee.catchdining.domain.menu.service.MenuService;
import com.jvnlee.catchdining.domain.payment.domain.PaymentType;
import com.jvnlee.catchdining.domain.payment.dto.PaymentDto;
import com.jvnlee.catchdining.domain.payment.dto.ReserveMenuDto;
import com.jvnlee.catchdining.domain.payment.service.PaymentService;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.restaurant.service.RestaurantService;
import com.jvnlee.catchdining.domain.seat.dto.SeatDto;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.model.SeatType;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import com.jvnlee.catchdining.domain.seat.service.SeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentIntegrationTest {

    @Autowired
    RestaurantService restaurantService;

    @Autowired
    SeatService seatService;

    @Autowired
    MenuService menuService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    SeatRepository seatRepository;

    @Test
    @DisplayName("100개의 결제 요청으로 예약 동시성 테스트")
    void create() throws InterruptedException {
        restaurantService.register(RestaurantDto.builder().name("r1").build());

        seatService.add(1L, new SeatDto(
                SeatType.BAR,
                List.of(LocalTime.of(13, 0, 0)),
                1,
                2,
                100
        ));

        menuService.add(1L, List.of(new MenuDto("sushi", 10000)));

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    paymentService.create(new PaymentDto(
                            1L,
                            List.of(new ReserveMenuDto(1L, 8000, 1)),
                            PaymentType.CREDIT_CARD
                    ));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Seat seat = seatRepository.findById(1L).orElseThrow();
        assertThat(seat.getAvailableQuantity()).isEqualTo(0);
    }
}
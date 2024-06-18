package com.jvnlee.catchdining.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.jvnlee.catchdining.domain.user.dto.UserLoginDto;
import com.jvnlee.catchdining.domain.user.service.UserService;
import com.jvnlee.catchdining.util.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jvnlee.catchdining.domain.user.model.UserType.OWNER;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@IntegrationTest
class ReservationIntegrationTest extends TestcontainersContext {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper om;

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

    final int THREAD_COUNT = 100;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("100개의 예약 요청으로 동시성 테스트")
    void create() throws Exception {
        for (int i = 1; i <= THREAD_COUNT; i++) {
            UserDto userJoinDto = new UserDto("user" + i, "12345", String.valueOf(i), OWNER);
            String userJoinRequestBody = om.writeValueAsString(userJoinDto);

            RestAssured
                    .given().log().all()
                    .body(userJoinRequestBody)
                    .contentType(JSON)
                    .when()
                    .post("/users")
                    .then().log().all();
        }

        UserLoginDto loginDto = new UserLoginDto("user1", "12345");
        String loginRequestBody = om.writeValueAsString(loginDto);

        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .body(loginRequestBody)
                .contentType(JSON)
                .when()
                .post("/login")
                .then().log().all()
                .extract();

        String authHeader = response.header(AUTHORIZATION);

        RestaurantDto restaurantCreateDto = RestaurantDto.builder().name("restaurant").build();
        String restaurantCreateRequestBody = om.writeValueAsString(restaurantCreateDto);

        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, authHeader)
                .body(restaurantCreateRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants")
                .then().log().all();

        SeatDto seatDto = new SeatDto(
                SeatType.BAR,
                List.of(LocalTime.of(13, 0, 0)),
                1,
                2,
                100
        );
        String seatAddRequestBody = om.writeValueAsString(seatDto);

        RestAssured
                .given().log().all()
                .pathParam("restaurantId", 1L)
                .header(AUTHORIZATION, authHeader)
                .body(seatAddRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants/{restaurantId}/seats")
                .then().log().all();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        ReservationDto reservationDto = new ReservationDto(
                1L,
                List.of(new ReserveMenuDto("Sushi", 8000, 1)),
                PaymentType.CREDIT_CARD,
                2
        );
        String reservationCreateRequestBody = om.writeValueAsString(reservationDto);

        for (int i = 1; i <= THREAD_COUNT; i++) {
            String username = "user" + i;
            executorService.submit(() -> {
                try {
                    UserLoginDto userLoginDto = new UserLoginDto(username, "12345");
                    String userLoginRequestBody = om.writeValueAsString(userLoginDto);

                    ExtractableResponse<Response> loginResponse = RestAssured
                            .given().log().all()
                            .body(userLoginRequestBody)
                            .contentType(JSON)
                            .when()
                            .post("/login")
                            .then().log().all()
                            .extract();

                    String authorizationHeader = loginResponse.header(AUTHORIZATION);

                    RestAssured
                            .given().log().all()
                            .header(AUTHORIZATION, authorizationHeader)
                            .body(reservationCreateRequestBody)
                            .contentType(JSON)
                            .when()
                            .post("/reservations")
                            .then().log().all();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
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
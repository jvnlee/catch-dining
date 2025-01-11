package com.jvnlee.catchdining.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.domain.payment.model.PaymentType;
import com.jvnlee.catchdining.domain.payment.dto.ReserveMenuDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationRequestDto;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.seat.dto.SeatDto;
import com.jvnlee.catchdining.domain.seat.model.SeatType;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserLoginDto;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jvnlee.catchdining.domain.user.model.UserType.OWNER;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

class ReservationIntegrationTest extends TestcontainersContext {

    @Autowired
    ObjectMapper om;

    final int THREAD_COUNT = 100;

    @Test
    @DisplayName("10개 수량의 좌석에 대한 100개의 예약 요청 동시성 테스트")
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

        String user1AuthHeader = response.header(AUTHORIZATION);

        RestaurantDto restaurantCreateDto = RestaurantDto.builder().name("restaurant").build();
        String restaurantCreateRequestBody = om.writeValueAsString(restaurantCreateDto);

        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, user1AuthHeader)
                .body(restaurantCreateRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants")
                .then().log().all()
                .extract();

        Long restaurantId = 1L;

        SeatDto seatDto = new SeatDto(
                SeatType.BAR,
                List.of(LocalTime.of(13, 0, 0)),
                1,
                2,
                10
        );
        String seatAddRequestBody = om.writeValueAsString(seatDto);

        RestAssured
                .given().log().all()
                .pathParam("restaurantId", restaurantId)
                .header(AUTHORIZATION, user1AuthHeader)
                .body(seatAddRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants/{restaurantId}/seats")
                .then().log().all();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

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

                    String authHeader = loginResponse.header(AUTHORIZATION);

                    TmpReservationRequestDto tmpReservationRequestDto = new TmpReservationRequestDto(1L);
                    String tmpReservationRequestRequestBody = om.writeValueAsString(tmpReservationRequestDto);

                    ExtractableResponse<Response> tmpReservationResponse = RestAssured
                            .given().log().all()
                            .header(AUTHORIZATION, authHeader)
                            .body(tmpReservationRequestRequestBody)
                            .contentType(JSON)
                            .when()
                            .post("/reservations/tmp")
                            .then().log().all()
                            .extract();

                    Object tmpRsvIdData = tmpReservationResponse.path("data.tmpRsvId");

                    if (tmpRsvIdData == null) {
                        return;
                    }

                    String tmpRsvId = tmpRsvIdData.toString();

                    ReservationRequestDto reservationRequestDto = new ReservationRequestDto(
                            tmpRsvId,
                            List.of(new ReserveMenuDto("Sushi", 8000, 1)),
                            PaymentType.CREDIT_CARD,
                            2
                    );
                    String reservationCreateRequestBody = om.writeValueAsString(reservationRequestDto);

                    RestAssured
                            .given().log().all()
                            .header(AUTHORIZATION, authHeader)
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

        RestAssured
                .given().log().all()
                .header(AUTHORIZATION, user1AuthHeader)
                .pathParam("restaurantId", restaurantId)
                .when()
                .get("/restaurants/{restaurantId}/reservations")
                .then().log().all()
                .assertThat()
                .body("data", hasSize(10));
    }
}
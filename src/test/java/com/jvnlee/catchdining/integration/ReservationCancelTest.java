package com.jvnlee.catchdining.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestDto;
import com.jvnlee.catchdining.domain.notification.model.DiningPeriod;
import com.jvnlee.catchdining.domain.payment.dto.ReserveMenuDto;
import com.jvnlee.catchdining.domain.payment.model.PaymentType;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import com.jvnlee.catchdining.domain.restaurant.dto.RestaurantDto;
import com.jvnlee.catchdining.domain.seat.dto.SeatDto;
import com.jvnlee.catchdining.domain.seat.model.SeatType;
import com.jvnlee.catchdining.domain.user.dto.UserDto;
import com.jvnlee.catchdining.domain.user.dto.UserLoginDto;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.jvnlee.catchdining.common.constant.RedisConstants.SEAT_AVAIL_QTY_PREFIX;
import static com.jvnlee.catchdining.common.constant.RedisConstants.TMP_RSV_SEAT_ID_PREFIX;
import static com.jvnlee.catchdining.domain.user.model.UserType.OWNER;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ReservationCancelTest extends TestcontainersContext {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper om;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @MockBean
    FirebaseMessaging firebaseMessaging;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("임시 예약 취소")
    void cancelTmp() throws Exception {
        UserDto userJoinDto = new UserDto("andy", "12345", "01012345678", OWNER);
        String userJoinRequestBody = om.writeValueAsString(userJoinDto);

        RestAssured
                .given().log().all()
                .body(userJoinRequestBody)
                .contentType(JSON)
                .when()
                .post("/users")
                .then().log().all();

        UserLoginDto loginDto = new UserLoginDto("andy", "12345");
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
                .then().log().all()
                .extract();

        Long restaurantId = 1L;
        int seatQuantity = 4;

        SeatDto seatDto = new SeatDto(
                SeatType.BAR,
                List.of(LocalTime.of(13, 0, 0)),
                1,
                2,
                seatQuantity
        );
        String seatAddRequestBody = om.writeValueAsString(seatDto);

        RestAssured
                .given().log().all()
                .pathParam("restaurantId", restaurantId)
                .header(AUTHORIZATION, authHeader)
                .body(seatAddRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants/{restaurantId}/seats")
                .then().log().all();

        Long seatId = 7L;
        TmpReservationRequestDto tmpReservationRequestDto = new TmpReservationRequestDto(seatId);
        String tmpReservationCreateRequestBody = om.writeValueAsString(tmpReservationRequestDto);

        ExtractableResponse<Response> tmpReservationResponse = RestAssured
                .given().log().all()
                .header(AUTHORIZATION, authHeader)
                .body(tmpReservationCreateRequestBody)
                .contentType(JSON)
                .when()
                .post("/reservations/tmp")
                .then().log().all()
                .extract();

        assertThat(redisTemplate.hasKey(SEAT_AVAIL_QTY_PREFIX + seatId)).isTrue();
        int availQtyCache = Integer.parseInt(redisTemplate.opsForValue().get(SEAT_AVAIL_QTY_PREFIX + seatId));
        assertThat(availQtyCache).isEqualTo(seatQuantity - 1);

        String tmpRsvId = tmpReservationResponse.path("data.tmpRsvId").toString();
        assertThat(redisTemplate.hasKey(TMP_RSV_SEAT_ID_PREFIX + tmpRsvId)).isTrue();

        RestAssured
                .given().log().all()
                .pathParam("tmpRsvId", tmpRsvId)
                .header(AUTHORIZATION, authHeader)
                .when()
                .delete("/reservations/tmp/{tmpRsvId}")
                .then().log().all();

        availQtyCache = Integer.parseInt(redisTemplate.opsForValue().get(SEAT_AVAIL_QTY_PREFIX + seatId));
        assertThat(availQtyCache).isEqualTo(seatQuantity);
        assertThat(redisTemplate.hasKey(TMP_RSV_SEAT_ID_PREFIX + tmpRsvId)).isFalse();
    }


    @Test
    @DisplayName("예약 취소")
    void cancel() throws Exception {
        UserDto userJoinDto = new UserDto("andy", "12345", "01012345678", OWNER);
        String userJoinRequestBody = om.writeValueAsString(userJoinDto);

        RestAssured
                .given().log().all()
                .body(userJoinRequestBody)
                .contentType(JSON)
                .when()
                .post("/users")
                .then().log().all();

        UserLoginDto loginDto = new UserLoginDto("andy", "12345");
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
                .then().log().all()
                .extract();

        Long restaurantId = 1L;
        int seatQuantity = 4;

        SeatDto seatDto = new SeatDto(
                SeatType.BAR,
                List.of(LocalTime.of(13, 0, 0)),
                1,
                2,
                seatQuantity
        );
        String seatAddRequestBody = om.writeValueAsString(seatDto);

        RestAssured
                .given().log().all()
                .pathParam("restaurantId", restaurantId)
                .header(AUTHORIZATION, authHeader)
                .body(seatAddRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants/{restaurantId}/seats")
                .then().log().all();

        Long seatId = 7L;
        TmpReservationRequestDto tmpReservationRequestDto = new TmpReservationRequestDto(seatId);
        String tmpReservationCreateRequestBody = om.writeValueAsString(tmpReservationRequestDto);

        ExtractableResponse<Response> tmpReservationResponse = RestAssured
                .given().log().all()
                .header(AUTHORIZATION, authHeader)
                .body(tmpReservationCreateRequestBody)
                .contentType(JSON)
                .when()
                .post("/reservations/tmp")
                .then().log().all()
                .extract();

        assertThat(redisTemplate.hasKey(SEAT_AVAIL_QTY_PREFIX + seatId)).isTrue();
        int availQtyCache = Integer.parseInt(redisTemplate.opsForValue().get(SEAT_AVAIL_QTY_PREFIX + seatId));
        assertThat(availQtyCache).isEqualTo(seatQuantity - 1);

        String tmpRsvId = tmpReservationResponse.path("data.tmpRsvId").toString();
        assertThat(redisTemplate.hasKey(TMP_RSV_SEAT_ID_PREFIX + tmpRsvId)).isTrue();

        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(
                tmpRsvId,
                List.of(new ReserveMenuDto("Pizza", 20000, 1)),
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

        NotificationRequestDto notificationRequestDto = new NotificationRequestDto(
                "fcm-token",
                LocalDate.now().plusDays(6),
                DiningPeriod.LUNCH,
                2
        );
        String notificationRequestBody = om.writeValueAsString(notificationRequestDto);

        RestAssured
                .given().log().all()
                .pathParam("restaurantId", restaurantId)
                .header(AUTHORIZATION, authHeader)
                .body(notificationRequestBody)
                .contentType(JSON)
                .when()
                .post("/restaurants/{restaurantId}/notificationRequests")
                .then().log().all()
                .extract();

        Long userId = 1L;
        Long reservationId = 1L;

        RestAssured
                .given().log().all()
                .pathParams("userId", userId, "reservationId", reservationId)
                .header(AUTHORIZATION, authHeader)
                .when()
                .put("/users/{userId}/reservations/{reservationId}")
                .then().log().all();

        availQtyCache = Integer.parseInt(redisTemplate.opsForValue().get(SEAT_AVAIL_QTY_PREFIX + seatId));
        assertThat(availQtyCache).isEqualTo(seatQuantity);

        assertThat(redisTemplate.hasKey(TMP_RSV_SEAT_ID_PREFIX + tmpRsvId)).isFalse();
        verify(firebaseMessaging, timeout(5000).atLeastOnce()).send(any(Message.class));

        RestAssured
                .given().log().all()
                .pathParam("userId", userId)
                .param("status", ReservationStatus.CANCELED)
                .header(AUTHORIZATION, authHeader)
                .when()
                .get("/users/{userId}/reservations")
                .then().log().all()
                .assertThat()
                .body("data", hasSize(1));
    }
}

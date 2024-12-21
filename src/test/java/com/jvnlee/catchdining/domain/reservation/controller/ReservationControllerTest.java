package com.jvnlee.catchdining.domain.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.common.exception.CacheInitializationException;
import com.jvnlee.catchdining.common.exception.InvalidRedisKeyException;
import com.jvnlee.catchdining.common.exception.NotEnoughSeatException;
import com.jvnlee.catchdining.domain.payment.dto.ReserveMenuDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRestaurantViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationStatusDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationUserViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationRequestDto;
import com.jvnlee.catchdining.domain.reservation.dto.TmpReservationResponseDto;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import com.jvnlee.catchdining.domain.reservation.service.ReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static com.jvnlee.catchdining.domain.payment.model.PaymentType.CREDIT_CARD;
import static com.jvnlee.catchdining.domain.reservation.model.ReservationStatus.RESERVED;
import static com.jvnlee.catchdining.domain.reservation.model.ReservationStatus.VISITED;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("controller-test")
@WebMvcTest(
        controllers = {ReservationController.class},
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@MockBean(JpaMetamodelMappingContext.class)
class ReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    ReservationService service;

    @Test
    @DisplayName("임시 예약 성공")
    void create_tmp_success() throws Exception {
        TmpReservationRequestDto tmpReservationRequestDto = new TmpReservationRequestDto(1L);
        String requestBody = om.writeValueAsString(tmpReservationRequestDto);

        when(service.createTmp(any(TmpReservationRequestDto.class))).thenReturn(new TmpReservationResponseDto("1234"));

        ResultActions resultActions = mockMvc.perform(
                post("/reservations/tmp")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(service).createTmp(any(TmpReservationRequestDto.class));
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("임시 예약 성공"))
                .andExpect(jsonPath("$.data.tmpRsvId").value("1234"));
    }

    @Test
    @DisplayName("임시 예약 실패: 자리 부족")
    void create_tmp_fail_not_enough_seat() throws Exception {
        TmpReservationRequestDto tmpReservationRequestDto = new TmpReservationRequestDto(1L);
        String requestBody = om.writeValueAsString(tmpReservationRequestDto);

        when(service.createTmp(any(TmpReservationRequestDto.class))).thenThrow(NotEnoughSeatException.class);

        ResultActions resultActions = mockMvc.perform(
                post("/reservations/tmp")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(service).createTmp(any(TmpReservationRequestDto.class));
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("잔여 좌석이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("임시 예약 실패: 캐시 초기화 과정에서 실패")
    void create_tmp_fail_cache_init() throws Exception {
        TmpReservationRequestDto tmpReservationRequestDto = new TmpReservationRequestDto(1L);
        String requestBody = om.writeValueAsString(tmpReservationRequestDto);

        when(service.createTmp(any(TmpReservationRequestDto.class)))
                .thenThrow(CacheInitializationException.class);

        ResultActions resultActions = mockMvc.perform(
                post("/reservations/tmp")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(service).createTmp(any(TmpReservationRequestDto.class));
        resultActions
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("요청 처리 도중 문제가 발생했습니다."));
    }

    @Test
    @DisplayName("예약 성공")
    void create_success() throws Exception {
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(
                "1234",
                List.of(new ReserveMenuDto("Sushi", 8000, 1)),
                CREDIT_CARD,
                2
        );
        String requestBody = om.writeValueAsString(reservationRequestDto);

        ResultActions resultActions = mockMvc.perform(
                post("/reservations")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(service).create(reservationRequestDto);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 성공"));
    }

    @Test
    @DisplayName("예약 실패: 유효하지 않은 임시 예약 키")
    void create_fail_invalid_tmp_rsv_seat_id_key() throws Exception {
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(
                "1234",
                List.of(new ReserveMenuDto("Sushi", 8000, 1)),
                CREDIT_CARD,
                2
        );
        String requestBody = om.writeValueAsString(reservationRequestDto);

        doThrow(new InvalidRedisKeyException()).when(service).create(reservationRequestDto);

        ResultActions resultActions = mockMvc.perform(
                post("/reservations")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 임시 예약 키입니다."));
    }

    @Test
    @DisplayName("예약 실패: 자리 부족")
    void create_fail_not_enough_seat() throws Exception {
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(
                "1234",
                List.of(new ReserveMenuDto("Sushi", 8000, 1)),
                CREDIT_CARD,
                2
        );
        String requestBody = om.writeValueAsString(reservationRequestDto);

        doThrow(new NotEnoughSeatException()).when(service).create(reservationRequestDto);

        ResultActions resultActions = mockMvc.perform(
                post("/reservations")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("잔여 좌석이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("사용자 기준 예약 내역 조회 성공")
    void viewByUser_success() throws Exception {
        Long userId = 1L;
        ReservationStatus status = RESERVED;
        List<ReservationUserViewDto> reservationList = List.of(
                new ReservationUserViewDto(
                        "restaurant",
                        LocalDateTime.of(2023, 1, 1, 13, 0, 0),
                        2
                )
        );

        when(service.viewByUser(userId, status)).thenReturn(reservationList);

        ResultActions resultActions = mockMvc.perform(
                get("/users/{userId}/reservations", userId)
                        .param("status", String.valueOf(status))
        );

        verify(service).viewByUser(userId, status);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 내역 조회 성공"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("식당 기준 예약 내역 조회 성공")
    void viewByRestaurant_success() throws Exception {
        Long restaurantId = 1L;
        ReservationStatus status = RESERVED;
        List<ReservationRestaurantViewDto> reservationList = List.of(
                new ReservationRestaurantViewDto(
                        "user",
                        LocalDateTime.of(2023, 1, 1, 13, 0, 0),
                        2
                )
        );

        when(service.viewByRestaurant(restaurantId, status)).thenReturn(reservationList);

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants/{restaurantId}/reservations", restaurantId)
                        .param("status", String.valueOf(status))
        );

        verify(service).viewByRestaurant(restaurantId, status);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 내역 조회 성공"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("식당에서 특정 예약 내역의 예약 상태 업데이트 성공")
    void updateStatus() throws Exception {
        Long restaurantId = 1L;
        Long reservationId = 1L;
        ReservationStatusDto reservationStatusDto = new ReservationStatusDto(VISITED);
        String requestBody = om.writeValueAsString(reservationStatusDto);

        ResultActions resultActions = mockMvc.perform(
                put("/restaurants/{restaurantId}/reservations/{reservationId}", restaurantId, reservationId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(service).updateStatus(reservationId, reservationStatusDto);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 상태 처리 완료"));
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancel() throws Exception {
        Long userId = 1L;
        Long reservationId = 1L;

        ResultActions resultActions = mockMvc.perform(
                put("/users/{userId}/reservations/{reservationId}", userId, reservationId)
        );

        verify(service).cancel(reservationId);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 취소 완료"));
    }

}
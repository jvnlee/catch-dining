package com.jvnlee.catchdining.domain.seat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.common.exception.SeatNotFoundException;
import com.jvnlee.catchdining.domain.seat.dto.SeatDto;
import com.jvnlee.catchdining.domain.seat.dto.SeatSearchDto;
import com.jvnlee.catchdining.domain.seat.model.SeatType;
import com.jvnlee.catchdining.domain.seat.service.SeatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.jvnlee.catchdining.domain.seat.model.SeatType.BAR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(
        controllers = {SeatController.class},
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@MockBean(JpaMetamodelMappingContext.class)
class SeatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    SeatService service;

    @Test
    @DisplayName("자리 등록 성공")
    void add() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "password", AuthorityUtils.createAuthorityList("ROLE_OWNER"));

        Long restaurantId = 1L;
        List<LocalTime> timeList = List.of(LocalTime.of(12, 0), LocalTime.of(13, 0));
        SeatDto seatDto = new SeatDto(BAR, timeList, 1, 2, 3);

        String requestBody = om.writeValueAsString(seatDto);

        ResultActions resultActions = mockMvc.perform(
                post("/restaurants/{restaurantId}/seats", restaurantId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(service).add(restaurantId, seatDto);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("자리 등록 성공"));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 성공")
    void search_success() throws Exception {
        Long restaurantId = 1L;
        LocalDate date = LocalDate.of(2023, 3, 25);
        SeatType seatType = BAR;
        int headCount = 2;

        List<SeatSearchDto> seatSearchDtoList = List.of(
                new SeatSearchDto(1L, LocalTime.of(12, 0))
        );

        when(service.search(restaurantId, date, seatType, headCount)).thenReturn(seatSearchDtoList);

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants/{restaurantId}/seats", restaurantId)
                        .param("date", date.toString())
                        .param("seatType", seatType.toString())
                        .param("headCount", String.valueOf(headCount))
        );

        verify(service).search(restaurantId, date, seatType, headCount);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 가능 목록 조회 결과"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("예약 가능 시간 조회 실패: 결과 없음")
    void search_fail() throws Exception {
        Long restaurantId = 1L;
        LocalDate date = LocalDate.of(2023, 3, 25);
        SeatType seatType = BAR;
        int headCount = 2;

        when(service.search(restaurantId, date, seatType, headCount)).thenThrow(SeatNotFoundException.class);

        ResultActions resultActions = mockMvc.perform(
                get("/restaurants/{restaurantId}/seats", restaurantId)
                        .param("date", date.toString())
                        .param("seatType", seatType.toString())
                        .param("headCount", String.valueOf(headCount))
        );

        verify(service).search(restaurantId, date, seatType, headCount);

        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("자리 정보가 존재하지 않습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

}
package com.jvnlee.catchdining.domain.seat.service;

import com.jvnlee.catchdining.common.exception.SeatNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import com.jvnlee.catchdining.domain.seat.dto.SeatDto;
import com.jvnlee.catchdining.domain.seat.dto.SeatSearchDto;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.jvnlee.catchdining.domain.seat.model.SeatType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    SeatRepository seatRepository;

    @Mock
    RestaurantRepository restaurantRepository;

    @InjectMocks
    SeatService seatService;

    @Test
    @DisplayName("자리 등록")
    void add() {
        Long restaurantId = 1L;
        List<LocalTime> timeList = List.of(LocalTime.of(12, 0), LocalTime.of(13, 0));
        SeatDto seatDto = new SeatDto(BAR, timeList, 1, 2, 3);

        Restaurant restaurant = Restaurant.builder()
                .name("r1")
                .build();

        when(restaurantRepository.findById(restaurantId))
                .thenReturn(Optional.of(restaurant));

        seatService.add(restaurantId, seatDto);

        verify(seatRepository, times(14)).save(any(Seat.class));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 성공")
    void search_success() {
        LocalDate date = LocalDate.of(2023, 3, 25);
        SeatSearchDto seatSearchDto = new SeatSearchDto();
        when(seatRepository.findTimeByCond(1L, date, BAR, 2)).thenReturn(List.of(seatSearchDto));

        List<SeatSearchDto> result = seatService.search(1L, date, BAR, 2);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("예약 가능 시간 조회 실패: 결과 없음")
    void search_fail() {
        LocalDate date = LocalDate.of(2023, 3, 25);
        when(seatRepository.findTimeByCond(1L, date, BAR, 2)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> seatService.search(1L, date, BAR, 2))
                .isInstanceOf(SeatNotFoundException.class);
    }

    @Test
    void updatePastDates() {
        seatService.updatePastDates();
        verify(seatRepository).updatePastDates(any(LocalDate.class), any(LocalDate.class));
    }

}
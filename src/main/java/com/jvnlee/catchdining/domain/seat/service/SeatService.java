package com.jvnlee.catchdining.domain.seat.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.common.exception.SeatNotFoundException;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import com.jvnlee.catchdining.domain.seat.dto.SeatDto;
import com.jvnlee.catchdining.domain.seat.dto.SeatSearchDto;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.model.SeatType;
import com.jvnlee.catchdining.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    private final RestaurantRepository restaurantRepository;

    private final int RESERVABLE_DATE_LIMIT = 7;

    public void add(Long restaurantId, SeatDto seatDto) {
        Restaurant restaurant = restaurantRepository
                .findById(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);

        List<LocalTime> availableTimes = seatDto.getAvailableTimes();

        for (LocalTime availableTime : availableTimes) {
            for (int i = 0; i < RESERVABLE_DATE_LIMIT; i++) {
                Seat seat = Seat.builder()
                        .restaurant(restaurant)
                        .seatType(seatDto.getSeatType())
                        .availableDate(LocalDate.now().plusDays(i))
                        .availableTime(availableTime)
                        .maxHeadCount(seatDto.getMaxHeadCount())
                        .quantity(seatDto.getQuantity())
                        .availableQuantity(seatDto.getAvailableQuantity())
                        .build();

                seatRepository.save(seat);
            }
        }
    }

    public List<SeatSearchDto> search(Long restaurantId, LocalDate date, SeatType seatType, int headCount) {
        List<SeatSearchDto> result = seatRepository.findTimeByCond(restaurantId, date, seatType, headCount);
        if (result.isEmpty()) throw new SeatNotFoundException();
        return result;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void updatePastDates() {
        seatRepository.updatePastDates(LocalDate.now().plusDays(RESERVABLE_DATE_LIMIT - 1), LocalDate.now());
    }

}
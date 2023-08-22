package com.jvnlee.catchdining.domain.reservation.dto;

import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReservationUserViewDto {

    private String restaurantName;

    private LocalDateTime time;

    private int headCount;

    public ReservationUserViewDto(Reservation reservation) {
        this.restaurantName = reservation.getRestaurant().getName();
        this.time = reservation.getTime();
        this.headCount = reservation.getHeadCount();
    }
}

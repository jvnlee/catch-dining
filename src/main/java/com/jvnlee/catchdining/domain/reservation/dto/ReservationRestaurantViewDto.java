package com.jvnlee.catchdining.domain.reservation.dto;

import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRestaurantViewDto {

    private String username;

    private LocalDateTime time;

    private int headCount;

    public ReservationRestaurantViewDto(Reservation reservation) {
        this.username = reservation.getUser().getUsername();
        this.time = reservation.getTime();
        this.headCount = reservation.getHeadCount();
    }
}

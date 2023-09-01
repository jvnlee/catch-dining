package com.jvnlee.catchdining.domain.reservation.dto;

import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatusDto {

    private ReservationStatus status;

}

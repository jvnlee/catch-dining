package com.jvnlee.catchdining.domain.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatSearchDto {

    private Long seatId;

    private LocalTime time;

}

package com.jvnlee.catchdining.domain.seat.dto;

import com.jvnlee.catchdining.domain.seat.model.SeatType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {

    private SeatType seatType;

    private List<LocalTime> availableTimes;

    private int minHeadCount;

    private int maxHeadCount;

    private int quantity;

}

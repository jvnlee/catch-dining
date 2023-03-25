package com.jvnlee.catchdining.domain.seat.dto;

import com.jvnlee.catchdining.domain.seat.model.SeatType;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class SeatDto {

    private SeatType seatType;

    private List<LocalTime> availableTimes;

    private int maxHeadCount;

    private int quantity;

    private int availableQuantity;

}

package com.jvnlee.catchdining.domain.reservation.event;

import com.jvnlee.catchdining.domain.notification.model.DiningPeriod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCancelledEvent {

    private Long restaurantId;

    private LocalDate date;

    private DiningPeriod diningPeriod;

    private int minHeadCount;

    private int maxHeadCount;

}

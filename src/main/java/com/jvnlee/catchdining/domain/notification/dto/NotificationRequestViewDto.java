package com.jvnlee.catchdining.domain.notification.dto;

import com.jvnlee.catchdining.domain.notification.model.NotificationRequest;
import com.jvnlee.catchdining.entity.DiningPeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestViewDto {

    private String restaurantName;

    private LocalDate desiredDate;

    private DiningPeriod diningPeriod;

    private int headCount;

    public NotificationRequestViewDto(NotificationRequest nr) {
        this.restaurantName = nr.getRestaurant().getName();
        this.desiredDate = nr.getDesiredDate();
        this.diningPeriod = nr.getDiningPeriod();
        this.headCount = nr.getHeadCount();
    }

}

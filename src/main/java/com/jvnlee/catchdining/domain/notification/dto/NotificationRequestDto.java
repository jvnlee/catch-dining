package com.jvnlee.catchdining.domain.notification.dto;

import com.jvnlee.catchdining.entity.DiningPeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {

    private String fcmToken;

    private LocalDate desiredDate;

    private DiningPeriod diningPeriod;

    private int headCount;

}

package com.jvnlee.catchdining.domain.notification.model;

import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestDto;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.entity.DiningPeriod;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;

import static javax.persistence.EnumType.*;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uc",
                columnNames = {"user_id", "restaurant_id", "desiredDate", "diningPeriod", "headCount"}
        )
})
@NoArgsConstructor(access = PROTECTED)
public class NotificationRequest {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "notification_request_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private LocalDate desiredDate;

    @Enumerated(STRING)
    private DiningPeriod diningPeriod;

    private int headCount;

    public NotificationRequest(User user, Restaurant restaurant, NotificationRequestDto notificationRequestDto) {
        this.user = user;
        this.restaurant = restaurant;
        this.desiredDate = notificationRequestDto.getDesiredDate();
        this.diningPeriod = notificationRequestDto.getDiningPeriod();
        this.headCount = notificationRequestDto.getHeadCount();
    }

}

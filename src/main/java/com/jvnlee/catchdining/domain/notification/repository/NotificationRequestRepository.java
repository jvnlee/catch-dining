package com.jvnlee.catchdining.domain.notification.repository;

import com.jvnlee.catchdining.domain.notification.model.NotificationRequest;
import com.jvnlee.catchdining.entity.DiningPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {

    @Query("select nr from NotificationRequest nr " +
            "where nr.restaurant.id = :restaurantId " +
            "and nr.desiredDate = :date " +
            "and nr.diningPeriod = :diningPeriod " +
            "and nr.headCount between :minHeadCount and :maxHeadCount")
    List<NotificationRequest> findAllByCond(Long restaurantId, LocalDate date, DiningPeriod diningPeriod, int minHeadCount, int maxHeadCount);

    @Query("select nr from NotificationRequest nr where nr.user.id = :userId")
    List<NotificationRequest> findAllByUserId(Long userId);

    @Modifying
    @Query("delete NotificationRequest nr where nr.desiredDate <= :baseDate")
    void clearPastRequests(LocalDate baseDate);

}

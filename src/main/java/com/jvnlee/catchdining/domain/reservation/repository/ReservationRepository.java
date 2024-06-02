package com.jvnlee.catchdining.domain.reservation.repository;

import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("select r from Reservation r where r.user.id = :userId and r.reservationStatus = :status")
    List<Reservation> findAllByUserIdAndStatus(Long userId, ReservationStatus status);

    @Query("select r from Reservation r where r.restaurant.id = :restaurantId and r.reservationStatus = :status")
    List<Reservation> findAllByRestaurantIdAndStatus(Long restaurantId, ReservationStatus status);

}

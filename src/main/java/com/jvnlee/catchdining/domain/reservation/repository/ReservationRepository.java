package com.jvnlee.catchdining.domain.reservation.repository;

import com.jvnlee.catchdining.domain.reservation.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}

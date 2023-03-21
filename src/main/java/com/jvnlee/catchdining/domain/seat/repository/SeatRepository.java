package com.jvnlee.catchdining.domain.seat.repository;

import com.jvnlee.catchdining.domain.seat.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}

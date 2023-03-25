package com.jvnlee.catchdining.domain.seat.repository;

import com.jvnlee.catchdining.domain.seat.dto.SeatSearchDto;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.seat.model.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("select new com.jvnlee.catchdining.domain.seat.dto.SeatSearchDto(s.id, s.availableTime) " +
            "from Seat s " +
            "where s.restaurant.id = :restaurantId " +
            "and s.availableDate = :date " +
            "and s.seatType = :seatType " +
            "and :headCount between s.minHeadCount and s.maxHeadCount")
    List<SeatSearchDto> findTimeByCond(Long restaurantId, LocalDate date, SeatType seatType, int headCount);

    @Modifying
    @Query("update Seat s " +
            "set s.availableDate = :date, " +
            "s.availableQuantity = s.quantity " +
            "where s.availableDate < :today")
    void updatePastDates(LocalDate date, LocalDate today);

}

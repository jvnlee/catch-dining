package com.jvnlee.catchdining.domain.seat.model;

import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private SeatType seatType;

    @Column(name = "available_date")
    private LocalDate availableDate;

    @Column(name = "available_time")
    private LocalTime availableTime;

    @Column(name = "min_head_count")
    private int minHeadCount;

    @Column(name = "max_head_count")
    private int maxHeadCount;

    private int quantity;

    @Column(name = "available_quantity")
    private int availableQuantity;

    public void occupy() {
        this.availableQuantity--;
    }

    public void release() {
        this.availableQuantity++;
    }

}

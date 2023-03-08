package com.jvnlee.catchdining.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "seat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    @ElementCollection
    @CollectionTable(name = "available_time",
            joinColumns = @JoinColumn(name = "seat_id"))
    private List<LocalDateTime> availabeTime;

    private int maxHeadCount;

}

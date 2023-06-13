package com.jvnlee.catchdining.domain.reservation.model;

import com.jvnlee.catchdining.domain.payment.domain.Payment;
import com.jvnlee.catchdining.domain.seat.model.Seat;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDateTime;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime time;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    private int headCount;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation(User user, LocalDateTime time, Seat seat, int headCount, Payment payment, ReservationStatus status) {
        this.user = user;
        this.time = time;
        this.seat = seat;
        this.headCount = headCount;
        this.payment = payment;
        this.status = status;
    }

}

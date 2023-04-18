package com.jvnlee.catchdining.domain.payment.domain;

import com.jvnlee.catchdining.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    private String tid;

    private int totalPrice;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    public Payment(String tid, int totalPrice, PaymentType paymentType) {
        this.tid = tid;
        this.totalPrice = totalPrice;
        this.paymentType = paymentType;
    }

}

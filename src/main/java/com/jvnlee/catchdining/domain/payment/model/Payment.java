package com.jvnlee.catchdining.domain.payment.model;

import com.jvnlee.catchdining.domain.BaseEntity;
import com.jvnlee.catchdining.undeveloped.ReserveMenu;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static com.jvnlee.catchdining.domain.payment.model.PaymentStatus.*;
import static javax.persistence.CascadeType.*;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "payment")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    private String tid;

    @Column(name = "total_price")
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "payment", cascade = ALL, orphanRemoval = true)
    private List<ReserveMenu> reserveMenus = new ArrayList<>();

    public Payment(String tid, int totalPrice, PaymentType paymentType, PaymentStatus paymentStatus) {
        this.tid = tid;
        this.totalPrice = totalPrice;
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
    }

    public void cancel() {
        this.paymentStatus = CANCELED;
    }

}

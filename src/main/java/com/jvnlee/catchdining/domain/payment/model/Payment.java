package com.jvnlee.catchdining.domain.payment.model;

import com.jvnlee.catchdining.entity.BaseEntity;
import com.jvnlee.catchdining.entity.ReserveMenu;
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
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    private String tid;

    @OneToMany(mappedBy = "payment", cascade = ALL)
    private List<ReserveMenu> reserveMenus = new ArrayList<>();

    private int totalPrice;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    public Payment(String tid, List<ReserveMenu> reserveMenus, int totalPrice, PaymentType paymentType, PaymentStatus paymentStatus) {
        this.tid = tid;
        this.reserveMenus = reserveMenus;
        this.totalPrice = totalPrice;
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
    }

    public void cancel() {
        this.paymentStatus = CANCELED;
    }

}

package com.jvnlee.catchdining.undeveloped;

import com.jvnlee.catchdining.domain.BaseEntity;
import com.jvnlee.catchdining.domain.payment.model.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "reserve_menu")
public class ReserveMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "reserve_menu_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(name = "menu_name")
    private String menuName;

    @Column(name = "reserve_price")
    private int reservePrice;

    private int quantity;

    public ReserveMenu(Payment payment, String menuName, int reservePrice, int quantity) {
        this.payment = payment;
        this.menuName = menuName;
        this.reservePrice = reservePrice;
        this.quantity = quantity;
    }

}

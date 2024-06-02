package com.jvnlee.catchdining.undeveloped;

import com.jvnlee.catchdining.domain.BaseEntity;
import com.jvnlee.catchdining.domain.menu.model.Menu;
import com.jvnlee.catchdining.domain.payment.model.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ReserveMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "reserve_menu_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @Column(name = "reserve_price")
    private int reservePrice;

    private int quantity;

    public ReserveMenu(Menu menu, int reservePrice, int quantity) {
        this.menu = menu;
        this.reservePrice = reservePrice;
        this.quantity = quantity;
    }

}

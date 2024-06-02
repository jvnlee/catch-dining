package com.jvnlee.catchdining.domain.menu.model;

import com.jvnlee.catchdining.domain.menu.dto.MenuDto;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    private String name;

    private int price;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public Menu(MenuDto menuDto, Restaurant restaurant) {
        this.name = menuDto.getName();
        this.price = menuDto.getPrice();
        this.restaurant = restaurant;
    }

    public void update(MenuDto menuDto) {
        this.name = menuDto.getName();
        this.price = menuDto.getPrice();
    }
}

package com.jvnlee.catchdining.domain.menu.dto;

import com.jvnlee.catchdining.domain.menu.domain.Menu;

public class MenuViewDto {

    private Long menuId;

    private String name;

    private int price;

    public MenuViewDto(Menu menu) {
        this.menuId = menu.getId();
        this.name = menu.getName();
        this.price = menu.getPrice();
    }

}

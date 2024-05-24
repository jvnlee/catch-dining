package com.jvnlee.catchdining.domain.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {

    private String name;

    private int price;

}

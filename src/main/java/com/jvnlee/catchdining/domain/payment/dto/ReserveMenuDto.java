package com.jvnlee.catchdining.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReserveMenuDto {

    private Long menuId;

    private int reservePrice;

    private int quantity;

}

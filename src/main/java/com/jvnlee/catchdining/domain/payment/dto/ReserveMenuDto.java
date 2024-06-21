package com.jvnlee.catchdining.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveMenuDto {

    private String menuName;

    private int reservePrice;

    private int quantity;

}

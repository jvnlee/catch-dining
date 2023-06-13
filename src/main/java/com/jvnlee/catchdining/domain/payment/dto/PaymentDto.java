package com.jvnlee.catchdining.domain.payment.dto;

import com.jvnlee.catchdining.domain.payment.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PaymentDto {

    private Long seatId;

    private List<ReserveMenuDto> reserveMenus;

    private PaymentType paymentType;

}

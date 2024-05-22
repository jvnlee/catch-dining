package com.jvnlee.catchdining.domain.payment.dto;

import com.jvnlee.catchdining.domain.payment.model.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private List<ReserveMenuDto> reserveMenus;

    private PaymentType paymentType;

}

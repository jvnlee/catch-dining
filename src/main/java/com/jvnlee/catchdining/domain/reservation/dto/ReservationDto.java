package com.jvnlee.catchdining.domain.reservation.dto;

import com.jvnlee.catchdining.domain.payment.model.PaymentType;
import com.jvnlee.catchdining.domain.payment.dto.ReserveMenuDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {

    private Long seatId;

    private List<ReserveMenuDto> reserveMenus;

    private PaymentType paymentType;

    private int headCount;

}

package com.jvnlee.catchdining.domain.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TmpReservationCancelRequestDto {

    private String tmpReservationKey;

}
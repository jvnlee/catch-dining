package com.jvnlee.catchdining.domain.reservation.controller;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationRestaurantViewDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationStatusDto;
import com.jvnlee.catchdining.domain.reservation.dto.ReservationUserViewDto;
import com.jvnlee.catchdining.domain.reservation.model.ReservationStatus;
import com.jvnlee.catchdining.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public Response<Void> create(@RequestBody ReservationDto reservationDto) {
        reservationService.create(reservationDto);
        return new Response<>("예약 성공");
    }

    @GetMapping("/users/{userId}/reservations")
    public Response<List<ReservationUserViewDto>> viewByUser(@PathVariable Long userId,
                                                             @RequestParam(defaultValue = "RESERVED") ReservationStatus status) {
        List<ReservationUserViewDto> data = reservationService.viewByUser(userId, status);
        return new Response<>("예약 내역 조회 성공", data);
    }

    @GetMapping("/restaurants/{restaurantId}/reservations")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public Response<List<ReservationRestaurantViewDto>> viewByRestaurant(@PathVariable Long restaurantId,
                                                                         @RequestParam(defaultValue = "RESERVED") ReservationStatus status) {
        List<ReservationRestaurantViewDto> data = reservationService.viewByRestaurant(restaurantId, status);
        return new Response<>("예약 내역 조회 성공", data);
    }

    @PutMapping("/restaurants/{restaurantId}/reservations/{reservationId}")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public Response<Void> updateStatus(@PathVariable Long reservationId, @RequestBody ReservationStatusDto reservationStatusDto) {
        reservationService.updateStatus(reservationId, reservationStatusDto);
        return new Response<>("예약 상태 처리 완료");
    }

    @PutMapping("/users/{userId}/reservations/{reservationId}")
    public Response<Void> cancel(@PathVariable Long reservationId) {
        reservationService.cancel(reservationId);
        return new Response<>("예약 취소 완료");
    }

}

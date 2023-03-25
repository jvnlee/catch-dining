package com.jvnlee.catchdining.domain.seat.controller;

import com.jvnlee.catchdining.common.exception.SeatNotFoundException;
import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.seat.dto.SeatDto;
import com.jvnlee.catchdining.domain.seat.dto.SeatSearchDto;
import com.jvnlee.catchdining.domain.seat.model.SeatType;
import com.jvnlee.catchdining.domain.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
@RequestMapping("/restaurants/{restaurantId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public Response<Void> add(@PathVariable Long restaurantId, @RequestBody SeatDto seatDto) {
        seatService.add(restaurantId, seatDto);
        return new Response<>("자리 등록 성공");
    }

    @GetMapping
    public Response<List<SeatSearchDto>> search(@PathVariable Long restaurantId,
                                                @RequestParam @DateTimeFormat(iso = DATE) LocalDate date,
                                                @RequestParam SeatType seatType,
                                                @RequestParam int headCount) {
        List<SeatSearchDto> data = seatService.search(restaurantId, date, seatType, headCount);
        return new Response<>("예약 가능 목록 조회 결과", data);
    }

    @ExceptionHandler(SeatNotFoundException.class)
    public Response<Void> handleSeatNotFound() {
        return new Response<>("자리 정보가 존재하지 않습니다.");
    }

}

package com.jvnlee.catchdining.common.advice;

import com.jvnlee.catchdining.common.exception.*;
import com.jvnlee.catchdining.common.web.Response;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<Void> handleDuplicateKey(DuplicateKeyException e) {
        return new Response<>(e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(BAD_REQUEST)
    public Response<Void> handleUserNotFound() {
        return new Response<>("존재하지 않는 사용자입니다.");
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public Response<Void> handleRestaurantNotFound() {
        return new Response<>("식당 정보가 존재하지 않습니다.");
    }

    @ExceptionHandler(SeatNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public Response<Void> handleSeatNotFound() {
        return new Response<>("자리 정보가 존재하지 않습니다.");
    }

    @ExceptionHandler(NotEnoughSeatException.class)
    @ResponseStatus(BAD_REQUEST)
    public Response<Void> handleNotEnoughSeat() {
        return new Response<>("잔여 좌석이 존재하지 않습니다.");
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public Response<Void> handleReservationNotFound() {
        return new Response<>("예약 내역이 존재하지 않습니다.");
    }

    @ExceptionHandler(FcmTokenNotFoundException.class)
    @ResponseStatus(BAD_REQUEST)
    public Response<Void> handleFcmTokenNotFound() {
        return new Response<>("유효한 FCM 토큰이 존재하지 않습니다.");
    }

    @ExceptionHandler(DuplicateNotificationRequestException.class)
    @ResponseStatus(BAD_REQUEST)
    public Response<Void> handleDuplicateNotificationRequest() {
        return new Response<>("이미 동일한 빈 자리 알림 신청 내역이 있습니다.");
    }

}
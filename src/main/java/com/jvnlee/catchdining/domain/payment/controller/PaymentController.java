package com.jvnlee.catchdining.domain.payment.controller;

import com.jvnlee.catchdining.common.exception.PaymentFailureException;
import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.payment.dto.PaymentDto;
import com.jvnlee.catchdining.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Response<Void> createPayment(@RequestBody PaymentDto paymentDto) {
        paymentService.create(paymentDto);
        return new Response<>("결제 성공");
    }

    @ExceptionHandler(PaymentFailureException.class)
    public Response<Void> handlePaymentFailure() {
        return new Response<>("결제 실패");
    }

}

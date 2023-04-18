package com.jvnlee.catchdining.domain.payment.service;

import static java.util.UUID.randomUUID;

// 가상의 외부 결제 모듈
public class FakePaymentModule {

    public String attemptPayment(int totalPrice) {
        // 결제 성공 시, UUID 문자열을 반환하고 결제 실패 시 빈 문자열을 반환하는 것으로 함
        // 결제 실패 조건: totalPrice가 10,000원인 경우
        return totalPrice == 10_000 ? "" : randomUUID().toString();
    }

}

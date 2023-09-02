package com.jvnlee.catchdining.domain.payment.repository;

import com.jvnlee.catchdining.domain.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

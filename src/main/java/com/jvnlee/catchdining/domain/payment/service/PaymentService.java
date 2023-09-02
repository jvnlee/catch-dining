package com.jvnlee.catchdining.domain.payment.service;

import com.jvnlee.catchdining.common.exception.PaymentFailureException;
import com.jvnlee.catchdining.common.exception.PaymentNotFoundException;
import com.jvnlee.catchdining.domain.menu.domain.Menu;
import com.jvnlee.catchdining.domain.menu.repository.MenuRepository;
import com.jvnlee.catchdining.domain.payment.model.Payment;
import com.jvnlee.catchdining.domain.payment.model.PaymentType;
import com.jvnlee.catchdining.domain.payment.dto.PaymentDto;
import com.jvnlee.catchdining.domain.payment.dto.ReserveMenuDto;
import com.jvnlee.catchdining.domain.payment.repository.PaymentRepository;
import com.jvnlee.catchdining.entity.ReserveMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.jvnlee.catchdining.domain.payment.model.PaymentStatus.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final MenuRepository menuRepository;

    private final FakePaymentModule fakePaymentModule;

    @Transactional(timeout = 300)
    public Payment create(PaymentDto paymentDto) {
        PaymentType paymentType = paymentDto.getPaymentType();
        List<ReserveMenuDto> reserveMenuDtoList = paymentDto.getReserveMenus();
        int total = reserveMenuDtoList
                .stream()
                .mapToInt(r -> r.getReservePrice() * r.getQuantity())
                .sum();

        // 외부 결제 API 호출 (가상으로 하기 위해 FakePaymentModule 활용)
        // tid: 결제 성공 시 받아오는 결제 고유 번호 문자열
        String tid = fakePaymentModule.attemptPayment(total);
        if (tid.isEmpty()) {
            throw new PaymentFailureException();
        }

        List<ReserveMenu> reserveMenuList = new ArrayList<>();
        for (ReserveMenuDto reserveMenuDto : reserveMenuDtoList) {
            Menu menu = menuRepository.findById(reserveMenuDto.getMenuId()).orElseThrow();
            ReserveMenu reserveMenu = new ReserveMenu(menu, reserveMenuDto.getReservePrice(), reserveMenuDto.getQuantity());
            reserveMenuList.add(reserveMenu);
        }

        Payment payment = new Payment(tid, reserveMenuList, total, paymentType, COMPLETE);

        return paymentRepository.save(payment);
    }

    public void cancel(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(PaymentNotFoundException::new);
        fakePaymentModule.attemptCancellation(payment.getTid());
        payment.cancel();
    }

}

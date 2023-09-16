package com.jvnlee.catchdining.domain.notification.controller;

import com.jvnlee.catchdining.common.web.Response;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestDto;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestViewDto;
import com.jvnlee.catchdining.domain.notification.service.NotificationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationRequestController {

    private final NotificationRequestService notificationRequestService;

    @PostMapping("/restaurants/{restaurantId}/notificationRequests")
    public Response<Void> request(@PathVariable Long restaurantId, @RequestBody NotificationRequestDto notificationRequestDto) {
        notificationRequestService.request(restaurantId, notificationRequestDto);
        return new Response<>("빈자리 알림 신청 완료");
    }

    @GetMapping("/users/{userId}/notificationRequests")
    public Response<List<NotificationRequestViewDto>> view(@PathVariable Long userId) {
        List<NotificationRequestViewDto> data = notificationRequestService.viewAll(userId);
        return new Response<>("빈자리 알림 신청 조회 성공", data);
    }

    @DeleteMapping("/users/{userId}/notificationRequests")
    public Response<Void> cancel(@RequestParam List<Long> id) {
        notificationRequestService.cancel(id);
        return new Response<>("빈자리 알림 신청 취소 완료");
    }

}

package com.jvnlee.catchdining.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jvnlee.catchdining.common.exception.DuplicateNotificationRequestException;
import com.jvnlee.catchdining.common.exception.FcmTokenNotFoundException;
import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestDto;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestViewDto;
import com.jvnlee.catchdining.domain.notification.model.NotificationRequest;
import com.jvnlee.catchdining.domain.notification.repository.NotificationRequestRepository;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import com.jvnlee.catchdining.domain.notification.model.DiningPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationRequestService {

    private final RestaurantRepository restaurantRepository;

    private final UserService userService;

    private final NotificationRequestRepository notificationRequestRepository;

    private final FirebaseMessagingService firebaseMessagingService;

    public void request(Long restaurantId, NotificationRequestDto notificationRequestDto) {
        String fcmToken = notificationRequestDto.getFcmToken();

        User user = userService.getCurrentUser();
        user.registerFcmToken(fcmToken);

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(RestaurantNotFoundException::new);

        NotificationRequest notificationRequest = new NotificationRequest(user, restaurant, notificationRequestDto);

        try {
            notificationRequestRepository.save(notificationRequest);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateNotificationRequestException();
        }
    }

    @Async
    public void notify(Long restaurantId, LocalDate date, DiningPeriod diningPeriod, int minHeadCount, int maxHeadCount) {
        List<NotificationRequest> list = notificationRequestRepository
                .findAllByCond(
                        restaurantId,
                        date,
                        diningPeriod,
                        minHeadCount,
                        maxHeadCount
                );

        if (list.isEmpty()) return;

        List<Long> userIdList = list
                .stream()
                .map(nr -> nr.getUser().getId())
                .collect(toList());

        List<String> fcmTokens = userService.getFcmTokens(userIdList);

        for (String fcmToken : fcmTokens) {
            if (fcmToken == null) {
                throw new FcmTokenNotFoundException();
            }

            Notification notification = Notification.builder()
                    .setTitle("빈 자리 알림")
                    .setBody("신청하신 시간대에 빈 자리가 생겼습니다. 지금 예약해보세요!")
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .build();

            try {
                firebaseMessagingService.send(message);
            } catch (FirebaseMessagingException e) {
                log.error(e.getErrorCode().toString() + ": " + e.getMessage());
            }
        }
    }

    public List<NotificationRequestViewDto> viewAll(Long userId) {
        return notificationRequestRepository.findAllByUserId(userId)
                .stream()
                .map(NotificationRequestViewDto::new)
                .collect(toList());
    }

    public void cancel(List<Long> idList) {
        notificationRequestRepository.deleteAllById(idList);
    }

    @Scheduled(cron = " 0 0 3 * * *")
    public void clearPastRequests() {
        notificationRequestRepository.clearPastRequests(LocalDate.now().minusDays(1));
    }

}

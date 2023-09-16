package com.jvnlee.catchdining.domain.notification.service;

import com.jvnlee.catchdining.common.exception.RestaurantNotFoundException;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestDto;
import com.jvnlee.catchdining.domain.notification.model.NotificationRequest;
import com.jvnlee.catchdining.domain.notification.repository.NotificationRequestRepository;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationRequestService {

    private final RestaurantRepository restaurantRepository;

    private final UserService userService;

    private final NotificationRequestRepository notificationRequestRepository;

    public void request(Long restaurantId, NotificationRequestDto notificationRequestDto) {
        String fcmToken = notificationRequestDto.getFcmToken();

        User user = userService.getCurrentUser();
        user.registerFcmToken(fcmToken);

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(RestaurantNotFoundException::new);

        NotificationRequest notificationRequest = new NotificationRequest(user, restaurant, notificationRequestDto);

        notificationRequestRepository.save(notificationRequest);
    }

}

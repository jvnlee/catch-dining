package com.jvnlee.catchdining.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jvnlee.catchdining.common.exception.DuplicateNotificationRequestException;
import com.jvnlee.catchdining.common.exception.FcmTokenNotFoundException;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestDto;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestViewDto;
import com.jvnlee.catchdining.domain.notification.model.NotificationRequest;
import com.jvnlee.catchdining.domain.notification.repository.NotificationRequestRepository;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.restaurant.repository.RestaurantRepository;
import com.jvnlee.catchdining.domain.user.model.User;
import com.jvnlee.catchdining.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.jvnlee.catchdining.domain.notification.model.DiningPeriod.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRequestServiceTest {

    @Mock
    RestaurantRepository restaurantRepository;

    @Mock
    UserService userService;

    @Mock
    NotificationRequestRepository notificationRequestRepository;

    @Mock
    FirebaseMessaging firebaseMessaging;

    @InjectMocks
    NotificationRequestService notificationRequestService;

    NotificationRequestDto notificationRequestDto;

    NotificationRequest notificationRequest;

    User user;

    Restaurant restaurant;

    @BeforeEach
    void beforeEach() {
        user = mock(User.class);
        restaurant = mock(Restaurant.class);

        notificationRequestDto = new NotificationRequestDto(
                "token",
                LocalDate.of(2023, 1, 1),
                LUNCH,
                2
        );

        notificationRequest = new NotificationRequest(user, restaurant, notificationRequestDto);
    }

    @Test
    @DisplayName("빈자리 알림 요청 성공")
    void request_success() {
        Long restaurantId = 1L;

        when(userService.getCurrentUser()).thenReturn(user);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));

        notificationRequestService.request(restaurantId, notificationRequestDto);

        verify(user).registerFcmToken(anyString());
        verify(notificationRequestRepository).save(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("빈자리 알림 요청 실패: 중복된 요청")
    void request_fail() {
        Long restaurantId = 1L;

        when(userService.getCurrentUser()).thenReturn(user);
        when(restaurantRepository.findById(anyLong())).thenReturn(Optional.of(restaurant));
        when(notificationRequestRepository.save(any(NotificationRequest.class))).thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> notificationRequestService.request(restaurantId, notificationRequestDto))
                .isInstanceOf(DuplicateNotificationRequestException.class);
    }

    @Test
    @DisplayName("알림 발행 성공")
    void notify_success() throws FirebaseMessagingException {
        Long restaurantId = 1L;
        LocalDate date = LocalDate.of(2023, 1, 1);

        when(notificationRequestRepository.findAllByCond(restaurantId, date, LUNCH, 1, 2))
                .thenReturn(List.of(notificationRequest));
        when(userService.getFcmTokens(anyList())).thenReturn(List.of("token"));

        notificationRequestService.notify(restaurantId, date, LUNCH, 1, 2);

        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    @DisplayName("알림 발행 실패: FCM 토큰 누락")
    void notify_fail_no_fcm_token() {
        Long restaurantId = 1L;
        LocalDate date = LocalDate.of(2023, 1, 1);

        List<String> fcmTokenList = new ArrayList<>();
        fcmTokenList.add("token");
        fcmTokenList.add(null);

        when(notificationRequestRepository.findAllByCond(restaurantId, date, LUNCH, 1, 2))
                .thenReturn(List.of(notificationRequest));
        when(userService.getFcmTokens(anyList())).thenReturn(fcmTokenList);

        assertThatThrownBy(() -> notificationRequestService.notify(restaurantId, date, LUNCH, 1, 2))
                .isInstanceOf(FcmTokenNotFoundException.class);
    }

    @Test
    @DisplayName("빈자리 알림 조회")
    void viewAll() {
        when(notificationRequestRepository.findAllByUserId(anyLong())).thenReturn(List.of(notificationRequest));

        assertThat(notificationRequestService.viewAll(1L)).hasSize(1);
        assertThat(notificationRequestService.viewAll(1L).get(0)).isInstanceOf(NotificationRequestViewDto.class);
    }

    @Test
    @DisplayName("빈자리 알림 신청 취소")
    void cancel() {
        List<Long> idList = List.of(1L, 2L, 3L);
        notificationRequestService.cancel(idList);
        verify(notificationRequestRepository).deleteAllById(idList);
    }
}
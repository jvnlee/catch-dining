package com.jvnlee.catchdining.domain.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvnlee.catchdining.common.exception.DuplicateNotificationRequestException;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestDto;
import com.jvnlee.catchdining.domain.notification.dto.NotificationRequestViewDto;
import com.jvnlee.catchdining.domain.notification.model.NotificationRequest;
import com.jvnlee.catchdining.domain.notification.service.NotificationRequestService;
import com.jvnlee.catchdining.domain.restaurant.model.Restaurant;
import com.jvnlee.catchdining.domain.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

import static com.jvnlee.catchdining.domain.notification.model.DiningPeriod.LUNCH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {NotificationRequestController.class},
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@MockBean(JpaMetamodelMappingContext.class)
class NotificationRequestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockBean
    NotificationRequestService service;

    NotificationRequestDto notificationRequestDto;

    NotificationRequest notificationRequest;

    @BeforeEach
    void beforeEach() {
        User user = mock(User.class);
        Restaurant restaurant = mock(Restaurant.class);

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
    void request_success() throws Exception {
        Long restaurantId = 1L;
        String requestBody = om.writeValueAsString(notificationRequestDto);

        ResultActions resultActions = mockMvc.perform(
                post("/restaurants/{restaurantId}/notificationRequests", restaurantId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        verify(service).request(restaurantId, notificationRequestDto);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("빈자리 알림 신청 완료"));
    }

    @Test
    @DisplayName("빈자리 알림 요청 실패: 중복된 요청")
    void request_fail() throws Exception {
        Long restaurantId = 1L;
        String requestBody = om.writeValueAsString(notificationRequestDto);

        doThrow(new DuplicateNotificationRequestException()).when(service).request(restaurantId, notificationRequestDto);

        ResultActions resultActions = mockMvc.perform(
                post("/restaurants/{restaurantId}/notificationRequests", restaurantId)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 동일한 빈 자리 알림 신청 내역이 있습니다."));
    }

    @Test
    @DisplayName("빈자리 알림 조회")
    void viewAll() throws Exception {
        Long userId = 1L;
        List<NotificationRequestViewDto> notificationRequestList = List.of(
                new NotificationRequestViewDto(notificationRequest)
        );

        when(service.viewAll(userId)).thenReturn(notificationRequestList);

        ResultActions resultActions = mockMvc.perform(
                get("/users/{userId}/notificationRequests", userId)
        );

        verify(service).viewAll(userId);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("빈자리 알림 신청 조회 성공"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("빈자리 알림 신청 취소")
    void cancel() throws Exception {
        Long userId = 1L;

        ResultActions resultActions = mockMvc.perform(
                delete("/users/{userId}/notificationRequests", userId)
                        .param("id", "1,2,3")
        );

        verify(service).cancel(anyList());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("빈자리 알림 신청 취소 완료"));
    }

}
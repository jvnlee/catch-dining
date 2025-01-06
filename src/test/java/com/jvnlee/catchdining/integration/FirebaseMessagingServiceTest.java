package com.jvnlee.catchdining.integration;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jvnlee.catchdining.common.exception.MessageSendingFailureException;
import com.jvnlee.catchdining.domain.notification.service.FirebaseMessagingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.annotation.EnableRetry;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableRetry
public class FirebaseMessagingServiceTest extends TestcontainersContext {

    @MockBean
    FirebaseMessaging firebaseMessaging;

    @Autowired
    FirebaseMessagingService firebaseMessagingService;

    @Test
    @DisplayName("FCM에 알림 전송 요청 실패 후 재시도 (3회)")
    void send_fail_retry() throws FirebaseMessagingException {
        when(firebaseMessaging.send(any(Message.class)))
                .thenThrow(FirebaseMessagingException.class);

        assertThatThrownBy(() -> firebaseMessagingService.send(mock(Message.class)))
                .isInstanceOf(MessageSendingFailureException.class);

        verify(firebaseMessaging, times(3)).send(any(Message.class));
    }

}

package com.jvnlee.catchdining.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.jvnlee.catchdining.common.exception.MessageSendingFailureException;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    @Retryable(
            value = MessageSendingFailureException.class,
            backoff = @Backoff(delay = 500L)
            // maxAttempts = 3 (default)
    )
    public void send(Message message) throws MessageSendingFailureException {
        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            throw new MessageSendingFailureException();
        }
    }

}

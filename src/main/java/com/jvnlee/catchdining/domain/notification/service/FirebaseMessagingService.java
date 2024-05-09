package com.jvnlee.catchdining.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    @Transactional
    @Retryable(
            value = FirebaseMessagingException.class,
            backoff = @Backoff(delay = 500L)
            // maxAttempts = 3 (default)
    )
    public void send(Message message) throws FirebaseMessagingException {
        firebaseMessaging.send(message);
    }

}

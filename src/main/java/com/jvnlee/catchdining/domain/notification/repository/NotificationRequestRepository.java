package com.jvnlee.catchdining.domain.notification.repository;

import com.jvnlee.catchdining.domain.notification.model.NotificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {
}

package com.parcelhub.notification_service.db.service;

import com.parcelhub.notification_service.db.model.NotificationDedupe;
import com.parcelhub.notification_service.db.repository.NotificationDedupeRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationDedupeService {

    private final NotificationDedupeRepository notificationDedupeRepository;

    public NotificationDedupeService(NotificationDedupeRepository notificationDedupeRepository) {
        this.notificationDedupeRepository = notificationDedupeRepository;
    }

    public boolean isAlreadySent(String businessKey) {
       return notificationDedupeRepository.existsByBusinessKey(businessKey);
    }

    public void markSent(String businessKey, UUID shipmentId) {
        notificationDedupeRepository.save(new NotificationDedupe(businessKey, shipmentId));
    }
}

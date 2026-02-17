package com.parcelhub.notification_service.db.service;

import com.parcelhub.notification_service.db.model.NotificationDedupe;
import com.parcelhub.notification_service.db.repository.NotificationDedupeRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationDedupeService {

    private final NotificationDedupeRepository notificationDedupeRepository;

    public NotificationDedupeService(NotificationDedupeRepository notificationDedupeRepository) {
        this.notificationDedupeRepository = notificationDedupeRepository;
    }

    @Transactional
    public boolean markSent(String businessKey, UUID shipmentId) {
        try {
            notificationDedupeRepository.saveAndFlush(new NotificationDedupe(businessKey, shipmentId));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}

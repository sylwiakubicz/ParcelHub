package com.parcelhub.notification_service.db.repository;

import com.parcelhub.notification_service.db.model.NotificationDedupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationDedupeRepository extends JpaRepository<NotificationDedupe, String> {
    boolean existsByBusinessKey(String businessKey);
}

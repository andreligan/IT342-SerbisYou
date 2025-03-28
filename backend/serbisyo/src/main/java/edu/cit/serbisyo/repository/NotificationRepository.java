package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    NotificationEntity findByMessage(String message);
}
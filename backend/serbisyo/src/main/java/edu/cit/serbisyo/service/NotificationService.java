package edu.cit.serbisyo.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.serbisyo.entity.NotificationEntity;
import edu.cit.serbisyo.repository.NotificationRepository;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public NotificationEntity createNotification(NotificationEntity notification) {
        return notificationRepository.save(notification);
    }

    public List<NotificationEntity> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public NotificationEntity updateNotification(Long notificationId, NotificationEntity updatedNotification) {
        NotificationEntity existingNotification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        existingNotification.setType(updatedNotification.getType());
        existingNotification.setMessage(updatedNotification.getMessage());
        existingNotification.setRead(updatedNotification.isRead());
        return notificationRepository.save(existingNotification);
    }

    public String deleteNotification(Long notificationId) {
        if (notificationRepository.existsById(notificationId)) {
            notificationRepository.deleteById(notificationId);
            return "Notification successfully deleted.";
        }
        return "Notification not found.";
    }
}

package edu.cit.serbisyo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import javax.naming.NameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.serbisyo.entity.NotificationEntity;
import edu.cit.serbisyo.repository.NotificationRepository;

@Service
public class NotificationService {
    @Autowired
    NotificationRepository nrepo;

    public NotificationService() {
        super();
    }

    // CREATE
    public NotificationEntity postNotification(NotificationEntity notification) {
        notification.setCreatedAt(LocalDateTime.now()); // Ensure createdAt is set on creation
        return nrepo.save(notification);
    }

    // READ
    public List<NotificationEntity> getAllNotifications() {
        return nrepo.findAll();
    }

    // UPDATE
    public NotificationEntity putNotificationDetails(int notificationId, NotificationEntity newNotificationDetails) throws NameNotFoundException {
        NotificationEntity notification = nrepo.findById(notificationId).orElseThrow(() -> new NoSuchElementException("Notification not found"));

        notification.setUserId(newNotificationDetails.getUserId());
        notification.setType(newNotificationDetails.getType());
        notification.setMessage(newNotificationDetails.getMessage());
        notification.setRead(newNotificationDetails.getRead());
        notification.setEntityType(newNotificationDetails.getEntityType());

        return nrepo.save(notification);
    }

    // DELETE
    public String deleteNotificationDetails(int notificationId) {
        String msg = "";
        if(nrepo.findById(notificationId).isPresent()) {
            nrepo.deleteById(notificationId);
            msg = "Notification record successfully deleted.";
        } else {
            return "Notification ID " + notificationId + " not found.";
        }
        return msg;
    }
}
package edu.cit.serbisyo.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.serbisyo.entity.NotificationEntity;
import edu.cit.serbisyo.repository.BookingRepository;
import edu.cit.serbisyo.repository.NotificationRepository;
import edu.cit.serbisyo.repository.ReviewRepository;
import edu.cit.serbisyo.repository.TransactionRepository;
import edu.cit.serbisyo.repository.MessageRepository;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MessageRepository messageRepository;

    public NotificationEntity createNotification(NotificationEntity notification) {
        return notificationRepository.save(notification);
    }

    public List<NotificationEntity> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public NotificationEntity updateNotification(Long notificationId, NotificationEntity updatedNotification) {
        NotificationEntity existingNotification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Update only the fields that are provided (not null)
        if (updatedNotification.getType() != null) {
            existingNotification.setType(updatedNotification.getType());
        }
        
        // Don't overwrite the message if it's null in the update
        if (updatedNotification.getMessage() != null) {
            existingNotification.setMessage(updatedNotification.getMessage());
        }
        
        // Always update the read status
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

    public Object getRelatedEntity(NotificationEntity notification) {
        String referenceType = notification.getReferenceType();
        Long referenceId = notification.getReferenceId();

        switch (referenceType) {
            case "Review":
                return reviewRepository.findById(referenceId);
            case "Booking":
                return bookingRepository.findById(referenceId);
            case "Transaction":
                return transactionRepository.findById(referenceId);
            case "Message": // New case for MessageEntity
                return messageRepository.findById(referenceId);
            default:
                throw new IllegalArgumentException("Unknown reference type: " + referenceType);
        }
    }
}

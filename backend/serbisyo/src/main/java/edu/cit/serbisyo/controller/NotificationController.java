package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.NotificationEntity;
import edu.cit.serbisyo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/print")
    public String print() {
        return "Notification Controller is working!";
    }

    // CREATE
    @PostMapping("/postNotification")
    public NotificationEntity postNotification(@RequestBody NotificationEntity notification) {
        return notificationService.postNotification(notification);
    }

    // READ ALL
    @GetMapping("/getAllNotifications")
    public List<NotificationEntity> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    // UPDATE
    @PutMapping("/putNotificationDetails")
    public NotificationEntity putNotificationDetails(@RequestParam int notificationId, @RequestBody NotificationEntity newNotificationDetails) {
        try {
            return notificationService.putNotificationDetails(notificationId, newNotificationDetails);
        } catch (Exception e) {
            throw new RuntimeException("Error updating notification: " + e.getMessage());
        }
    }

    // DELETE
    @DeleteMapping("/deleteNotificationDetails/{notificationId}")
    public String deleteNotificationDetails(@PathVariable int notificationId) {
        return notificationService.deleteNotificationDetails(notificationId);
    }
}
package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import edu.cit.serbisyo.entity.NotificationEntity;
import edu.cit.serbisyo.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/create")
    public NotificationEntity createNotification(@RequestBody NotificationEntity notification) {
        return notificationService.createNotification(notification);
    }

    @GetMapping("/getAll")
    public List<NotificationEntity> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @PutMapping("/update/{notificationId}")
    public NotificationEntity updateNotification(@PathVariable Long notificationId, @RequestBody NotificationEntity updatedNotification) {
        return notificationService.updateNotification(notificationId, updatedNotification);
    }

    @DeleteMapping("/delete/{notificationId}")
    public String deleteNotification(@PathVariable Long notificationId) {
        return notificationService.deleteNotification(notificationId);
    }
}

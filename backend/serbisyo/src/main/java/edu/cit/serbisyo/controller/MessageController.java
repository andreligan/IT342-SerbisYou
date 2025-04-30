package edu.cit.serbisyo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.serbisyo.entity.MessageEntity;
import edu.cit.serbisyo.service.MessageService;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/print")
    public String print() {
        return "Message Controller is working!";
    }

    @PostMapping("/postMessage")
    public MessageEntity createMessage(@RequestBody MessageEntity message) {
        return messageService.createMessage(message);
    }

    @GetMapping("/getAll")
    public List<MessageEntity> getAllMessages() {
        return messageService.getAllMessages();
    }

    @PutMapping("/updateMessage/{messageId}")
    public MessageEntity updateMessage(@PathVariable Long messageId, @RequestBody MessageEntity updatedMessage) {
        return messageService.updateMessage(messageId, updatedMessage);
    }

    @DeleteMapping("/delete/{messageId}")
    public String deleteMessage(@PathVariable Long messageId) {
        return messageService.deleteMessage(messageId);
    }
    
    @GetMapping("/{messageId}/status")
    public ResponseEntity<?> getMessageStatus(@PathVariable Long messageId) {
        try {
            MessageEntity message = messageService.getMessage(messageId);
            return ResponseEntity.ok(Map.of("status", message.getStatus()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving message status: " + e.getMessage());
        }
    }
    
    @PutMapping("/{messageId}/status")
    public ResponseEntity<?> updateMessageStatus(
            @PathVariable Long messageId,
            @RequestParam String status) {
        try {
            MessageEntity message = messageService.updateMessageStatus(messageId, status);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating message status: " + e.getMessage());
        }
    }
    
    @GetMapping("/conversation/{userId1}/{userId2}")
    public List<MessageEntity> getConversationBetweenUsers(
        @PathVariable Long userId1, 
        @PathVariable Long userId2) {
        return messageService.getConversationBetweenUsers(userId1, userId2);
    }

    @GetMapping("/conversation-partners/{userId}")
    public List<Map<String, Object>> getConversationPartners(@PathVariable Long userId) {
        return messageService.getConversationPartners(userId);
    }
}

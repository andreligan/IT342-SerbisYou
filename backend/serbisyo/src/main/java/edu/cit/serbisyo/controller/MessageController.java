package edu.cit.serbisyo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
}

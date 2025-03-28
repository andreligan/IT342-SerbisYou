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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.serbisyo.entity.MessageEntity;
import edu.cit.serbisyo.service.MessageService;

@RestController
@RequestMapping(method=RequestMethod.GET, path="/api/messages")
public class MessageController {

    @Autowired
    MessageService messageService;

    @GetMapping("/print")
    public String print() {
        return "Wow, it works!";
    }
    
    // CREATE
    @PostMapping("/postMessage")
    public MessageEntity postMessage(@RequestBody MessageEntity message) {
        return messageService.postMessage(message);
    }
    
    // READ
    @GetMapping("/getAllMessages")
    public List<MessageEntity> getAllMessages() {
        return messageService.getAllMessages();
    }

    // UPDATE
    @PutMapping("/putMessageDetails")
    public MessageEntity putMessageDetails(@RequestParam int messageId, @RequestBody MessageEntity newMessageDetails) {
        return messageService.putMessageDetails(messageId, newMessageDetails);
    }

    // DELETE
    @DeleteMapping("/deleteMessageDetails/{messageId}")
    public String deleteMessageDetails(@PathVariable int messageId) {
        return messageService.deleteMessageDetails(messageId);
    }
}
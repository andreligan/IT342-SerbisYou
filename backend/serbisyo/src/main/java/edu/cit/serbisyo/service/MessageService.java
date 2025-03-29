package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.MessageEntity;
import edu.cit.serbisyo.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageEntity createMessage(MessageEntity message) {
        return messageRepository.save(message);
    }

    public List<MessageEntity> getAllMessages() {
        return messageRepository.findAll();
    }

    public MessageEntity updateMessage(Long messageId, MessageEntity updatedMessage) {
        MessageEntity existingMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        existingMessage.setMessageText(updatedMessage.getMessageText());
        existingMessage.setStatus(updatedMessage.getStatus());

        return messageRepository.save(existingMessage);
    }

    public String deleteMessage(Long messageId) {
        if (messageRepository.existsById(messageId)) {
            messageRepository.deleteById(messageId);
            return "Message successfully deleted.";
        }
        return "Message not found.";
    }
}

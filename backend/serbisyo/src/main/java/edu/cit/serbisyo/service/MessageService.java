package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.MessageEntity;
import edu.cit.serbisyo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository mrepo;

    public MessageEntity postMessage(MessageEntity message) {
        message.setSentAt(LocalDateTime.now()); // Ensure sentAt is set on creation
        return mrepo.save(message);
    }

    public List<MessageEntity> getAllMessages() {
        return mrepo.findAll();
    }

    public Optional<MessageEntity> getMessageById(int messageId) {
        return mrepo.findById(messageId);
    }

    public MessageEntity putMessageDetails(int messageId, MessageEntity newMessageDetails) {
        Optional<MessageEntity> existingMessageOptional = mrepo.findById(messageId);
        if (existingMessageOptional.isPresent()) {
            MessageEntity existingMessage = existingMessageOptional.get();
            existingMessage.setSenderId(newMessageDetails.getSenderId());
            existingMessage.setReceiverId(newMessageDetails.getReceiverId());
            existingMessage.setBookingId(newMessageDetails.getBookingId());
            existingMessage.setMessageText(newMessageDetails.getMessageText());
            existingMessage.setStatus(newMessageDetails.getStatus());
            // Note: We are not allowing updates to sentAt
            return mrepo.save(existingMessage);
        } else {
            return null; // Or throw an exception
        }
    }

      // DELETE
      public String deleteMessageDetails(int messageId) {
        String msg = "";
        if(mrepo.findById(messageId).isPresent()) {
            mrepo.deleteById(messageId);
            msg = "Address Record sucessfully deleted.";
        } else {
            return "User Authentication ID " + messageId + " not found.";
        }
        return msg;
    }
}
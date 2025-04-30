package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.MessageEntity;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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

    public MessageEntity getMessage(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with ID: " + messageId));
    }

    public MessageEntity updateMessage(Long messageId, MessageEntity updatedMessage) {
        MessageEntity existingMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        existingMessage.setMessageText(updatedMessage.getMessageText());
        existingMessage.setStatus(updatedMessage.getStatus());

        return messageRepository.save(existingMessage);
    }

    public MessageEntity updateMessageStatus(Long messageId, String status) {
        MessageEntity existingMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with ID: " + messageId));
        
        // Validate status
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid message status: " + status);
        }
        
        existingMessage.setStatus(status.toUpperCase());
        return messageRepository.save(existingMessage);
    }
    
    private boolean isValidStatus(String status) {
        if (status == null) {
            return false;
        }
        
        String upperStatus = status.toUpperCase();
        return "SENT".equals(upperStatus) || 
               "DELIVERED".equals(upperStatus) || 
               "READ".equals(upperStatus);
    }

    public String deleteMessage(Long messageId) {
        if (messageRepository.existsById(messageId)) {
            messageRepository.deleteById(messageId);
            return "Message successfully deleted.";
        }
        return "Message not found.";
    }
    
    public List<MessageEntity> getConversationBetweenUsers(Long userId1, Long userId2) {
        return messageRepository.findBySenderUserIdAndReceiverUserIdOrSenderUserIdAndReceiverUserId(
            userId1, userId2, userId2, userId1);
    }

    public List<Map<String, Object>> getConversationPartners(Long userId) {
        List<MessageEntity> allMessages = messageRepository.findBySenderUserIdOrReceiverUserId(userId, userId);
        
        Map<Long, MessageEntity> latestMessageByPartner = new HashMap<>();
        Map<Long, UserAuthEntity> partners = new HashMap<>();
        
        for (MessageEntity message : allMessages) {
            boolean isCurrentUserSender = message.getSender().getUserId().equals(userId);
            UserAuthEntity partner = isCurrentUserSender ? message.getReceiver() : message.getSender();
            Long partnerId = partner.getUserId();
            
            if (partnerId.equals(userId)) {
                continue;
            }
            
            partners.putIfAbsent(partnerId, partner);
            
            MessageEntity existingLatest = latestMessageByPartner.get(partnerId);
            if (existingLatest == null || message.getSentAt().isAfter(existingLatest.getSentAt())) {
                latestMessageByPartner.put(partnerId, message);
            }
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map.Entry<Long, UserAuthEntity> entry : partners.entrySet()) {
            Long partnerId = entry.getKey();
            UserAuthEntity partner = entry.getValue();
            MessageEntity latestMessage = latestMessageByPartner.get(partnerId);
            
            Map<String, Object> partnerData = new HashMap<>();
            partnerData.put("userId", partnerId);
            partnerData.put("userName", partner.getUserName());
            partnerData.put("role", partner.getRole());
            
            if (partner.getCustomer() != null) {
                partnerData.put("firstName", partner.getCustomer().getFirstName());
                partnerData.put("lastName", partner.getCustomer().getLastName());
                if (partner.getCustomer().getProfileImage() != null) {
                    partnerData.put("profileImage", partner.getCustomer().getProfileImage());
                }
            } else if (partner.getServiceProvider() != null) {
                partnerData.put("firstName", partner.getServiceProvider().getFirstName());
                partnerData.put("lastName", partner.getServiceProvider().getLastName());
                partnerData.put("businessName", partner.getServiceProvider().getBusinessName());
            }
            
            partnerData.put("lastMessage", latestMessage.getMessageText());
            partnerData.put("lastMessageTime", latestMessage.getSentAt());
            partnerData.put("isUnread", userId.equals(latestMessage.getReceiver().getUserId()) && 
                                        !"READ".equals(latestMessage.getStatus()));
            
            result.add(partnerData);
        }
        
        result.sort((a, b) -> ((LocalDateTime)b.get("lastMessageTime"))
                    .compareTo((LocalDateTime)a.get("lastMessageTime")));
        
        return result;
    }
}

package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    
    MessageEntity findByMessageText(String messageText);
    
    @Query("SELECT m FROM MessageEntity m WHERE " +
           "(m.sender.userId = :senderId AND m.receiver.userId = :receiverId) OR " +
           "(m.sender.userId = :alternateSenderId AND m.receiver.userId = :alternateReceiverId) " +
           "ORDER BY m.sentAt ASC")
    List<MessageEntity> findBySenderUserIdAndReceiverUserIdOrSenderUserIdAndReceiverUserId(
        @Param("senderId") Long senderId,
        @Param("receiverId") Long receiverId,
        @Param("alternateSenderId") Long alternateSenderId,
        @Param("alternateReceiverId") Long alternateReceiverId);
        
    // New method to find all messages for a specific user
    @Query("SELECT m FROM MessageEntity m WHERE " +
           "m.sender.userId = :userId OR m.receiver.userId = :userId " +
           "ORDER BY m.sentAt DESC")
    List<MessageEntity> findBySenderUserIdOrReceiverUserId(
        @Param("userId") Long userId,
        @Param("userId") Long sameUserId);
}
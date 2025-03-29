package edu.cit.serbisyo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "Message")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "senderId", nullable = false)
    private UserAuthEntity sender;

    @ManyToOne
    @JoinColumn(name = "receiverId", nullable = false)
    private UserAuthEntity receiver;

    private String messageText;
    private LocalDateTime sentAt;
    private String status;

    // Getters and Setters

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public UserAuthEntity getSender() {
        return sender;
    }

    public void setSender(UserAuthEntity sender) {
        this.sender = sender;
    }

    public UserAuthEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(UserAuthEntity receiver) {
        this.receiver = receiver;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
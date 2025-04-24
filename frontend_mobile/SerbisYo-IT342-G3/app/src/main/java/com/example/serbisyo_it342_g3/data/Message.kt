package com.example.serbisyo_it342_g3.data

import java.util.Date

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    ERROR
}

data class Message(
    val messageId: Long? = null,
    val senderId: Long,
    val recipientId: Long,
    val content: String,
    val timestamp: Date = Date(),
    val read: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT,
    val senderName: String? = null,
    val senderRole: String? = null,
    val senderProfileImage: String? = null,
    val isTemporary: Boolean = false
)
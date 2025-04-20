package com.example.serbisyo_it342_g3.data

import java.util.Date

data class Message(
    val messageId: Long? = null,
    val senderId: Long,
    val recipientId: Long,
    val content: String,
    val timestamp: Date = Date(),
    val read: Boolean = false,
    val senderName: String? = null,
    val senderRole: String? = null,
    val senderProfileImage: String? = null
)

data class Conversation(
    val id: String,
    val userId: Long,
    val userName: String,
    val userRole: String,
    val lastMessage: String,
    val lastMessageTime: Date,
    val unreadCount: Int,
    val profileImage: String? = null
) 
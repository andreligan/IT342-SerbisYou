package com.example.serbisyo_it342_g3.data

import java.util.Date

data class Conversation(
    val id: String,
    val userId: Long,
    val userName: String,
    val userRole: String,
    val profileImage: String? = null,
    val lastMessage: String,
    val lastMessageTime: Date,
    val unreadCount: Int = 0
) 
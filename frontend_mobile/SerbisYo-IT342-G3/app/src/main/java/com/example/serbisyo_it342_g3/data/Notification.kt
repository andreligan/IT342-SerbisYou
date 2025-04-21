package com.example.serbisyo_it342_g3.data

import java.util.Date

data class Notification(
    val notificationId: Long = 0,
    val userId: Long = 0,  // The user who will receive the notification
    val type: String? = null,
    val message: String? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null  // Represented as ISO date string in format yyyy-MM-dd'T'HH:mm:ss
) 
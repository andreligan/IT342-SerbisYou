package com.example.serbisyo_it342_g3.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Notification(
    @SerializedName("notificationId") val notificationId: Long = 0,
    @SerializedName("userId") val userId: Long? = null,
    @SerializedName("user") val user: Any? = null, // Changed to Any to handle both UserData and Map<String, Any>
    @SerializedName("type") val type: String? = "", // Changed to nullable
    @SerializedName("message") val message: String? = "", // Changed to nullable
    @SerializedName("isRead") val isRead: Boolean = false,
    @SerializedName("read") val read: Boolean = false, // Backend might return either isRead or read
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("referenceId") val referenceId: Long = 0,
    @SerializedName("referenceType") val referenceType: String = "",
    @SerializedName("senderName") val senderName: String = "", // Added for UI display
    @SerializedName("senderProfileImage") val senderProfileImage: String? = null, // Added for UI display
    @SerializedName("senderId") val senderId: Long = 0 // Added to identify the original sender
) : Serializable

// Simple data class to parse nested user object from server
data class UserData(
    @SerializedName("userId") val userId: Long = 0,
    @SerializedName("userName") val userName: String = "",
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null
) : Serializable
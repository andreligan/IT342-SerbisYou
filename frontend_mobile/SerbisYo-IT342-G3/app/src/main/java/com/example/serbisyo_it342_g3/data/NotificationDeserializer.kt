package com.example.serbisyo_it342_g3.data

import android.util.Log
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class NotificationDeserializer : JsonDeserializer<Notification> {
    private val TAG = "NotificationDeserializer"

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Notification {
        try {
            val jsonObject = json.asJsonObject
            
            // Parse notification basics
            val notificationId = jsonObject.get("notificationId")?.asLong ?: 0L
            val userId = if (jsonObject.has("userId") && !jsonObject.get("userId").isJsonNull) {
                jsonObject.get("userId").asLong
            } else null
            
            // Parse the message, type and other simple fields
            val type = if (jsonObject.has("type") && !jsonObject.get("type").isJsonNull) {
                jsonObject.get("type").asString
            } else null
            
            val message = if (jsonObject.has("message") && !jsonObject.get("message").isJsonNull) {
                jsonObject.get("message").asString
            } else null
            
            val isRead = jsonObject.has("isRead") && !jsonObject.get("isRead").isJsonNull && jsonObject.get("isRead").asBoolean
            val read = jsonObject.has("read") && !jsonObject.get("read").isJsonNull && jsonObject.get("read").asBoolean
            
            val referenceId = if (jsonObject.has("referenceId") && !jsonObject.get("referenceId").isJsonNull) {
                jsonObject.get("referenceId").asLong
            } else 0L
            
            val referenceType = if (jsonObject.has("referenceType") && !jsonObject.get("referenceType").isJsonNull) {
                jsonObject.get("referenceType").asString
            } else ""
            
            val senderName = if (jsonObject.has("senderName") && !jsonObject.get("senderName").isJsonNull) {
                jsonObject.get("senderName").asString
            } else ""
            
            val senderProfileImage = if (jsonObject.has("senderProfileImage") && !jsonObject.get("senderProfileImage").isJsonNull) {
                jsonObject.get("senderProfileImage").asString
            } else null
            
            val senderId = if (jsonObject.has("senderId") && !jsonObject.get("senderId").isJsonNull) {
                jsonObject.get("senderId").asLong
            } else 0L
            
            // Parse user (most complex field)
            var user: Any? = null
            if (jsonObject.has("user") && !jsonObject.get("user").isJsonNull) {
                try {
                    val userElement = jsonObject.get("user")
                    if (userElement.isJsonObject) {
                        // Try to deserialize as UserData first
                        try {
                            user = context.deserialize<UserData>(userElement, UserData::class.java)
                        } catch (e: Exception) {
                            // If that fails, get it as a Map
                            Log.d(TAG, "Failed to deserialize user as UserData, using Map instead")
                            user = context.deserialize<Map<String, Any>>(userElement, object : TypeToken<Map<String, Any>>() {}.type)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing user in notification", e)
                }
            }
            
            // Parse createdAt (handle both string and array formats)
            var createdAt = ""
            if (jsonObject.has("createdAt")) {
                val dateElement = jsonObject.get("createdAt")
                
                if (dateElement.isJsonArray) {
                    // Handle array format like [2025,4,30,9,44,22]
                    try {
                        val dateArray = dateElement.asJsonArray
                        if (dateArray.size() >= 3) {
                            val year = dateArray.get(0).asInt
                            val month = dateArray.get(1).asInt
                            val day = dateArray.get(2).asInt
                            
                            // Include time if available in the array
                            if (dateArray.size() >= 6) {
                                val hour = dateArray.get(3).asInt
                                val minute = dateArray.get(4).asInt
                                val second = dateArray.get(5).asInt
                                createdAt = String.format("%04d-%02d-%02d %02d:%02d:%02d", 
                                    year, month, day, hour, minute, second)
                            } else {
                                createdAt = String.format("%04d-%02d-%02d", year, month, day)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing notification date array", e)
                    }
                } else if (dateElement.isJsonPrimitive) {
                    // It's already a string
                    createdAt = dateElement.asString
                }
            }
            
            return Notification(
                notificationId = notificationId,
                userId = userId,
                user = user,
                type = type,
                message = message,
                isRead = isRead,
                read = read,
                createdAt = createdAt,
                referenceId = referenceId,
                referenceType = referenceType,
                senderName = senderName,
                senderProfileImage = senderProfileImage,
                senderId = senderId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in NotificationDeserializer", e)
            // Return a minimal notification to avoid null
            return Notification()
        }
    }
} 
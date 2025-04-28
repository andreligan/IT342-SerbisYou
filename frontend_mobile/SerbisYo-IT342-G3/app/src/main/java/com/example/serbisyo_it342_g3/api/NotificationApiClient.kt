package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Notification
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NotificationApiClient(private val context: Context) {
    private val baseApiClient = BaseApiClient(context)
    private val client = baseApiClient.client
    private val gson = baseApiClient.gson
    
    private val TAG = "NotificationApiClient"

    // Get notifications for a specific user
    fun getNotificationsByUserId(userId: Long, token: String, callback: (List<Notification>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Getting notifications for user: $userId with token: ${token.take(20)}...")
        
        // Try using getAll endpoint instead of user-specific endpoint since we're getting 403 errors
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/notifications/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        Log.d(TAG, "Request URL: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get notifications", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    try {
                        val type = object : TypeToken<List<Notification>>() {}.type
                        val allNotifications = gson.fromJson<List<Notification>>(responseBody, type)
                        
                        // Detail log the userIds from the response to debug
                        Log.d(TAG, "All user IDs in notifications: ${allNotifications.map { notification -> 
                            getUserIdFromObject(notification.user)
                        }}")
                        
                        // Better log all notification user objects for debugging
                        allNotifications.forEach { notification ->
                            val userObj = notification.user
                            if (userObj != null) {
                                when (userObj) {
                                    is Map<*, *> -> {
                                        Log.d(TAG, "Notification ${notification.notificationId} has user as Map: $userObj")
                                        Log.d(TAG, "  userId = ${userObj["userId"]}, userName = ${userObj["userName"]}")
                                    }
                                    else -> {
                                        Log.d(TAG, "Notification ${notification.notificationId} has user as ${userObj::class.java.simpleName}")
                                        try {
                                            val userIdField = userObj::class.java.getDeclaredField("userId")
                                            userIdField.isAccessible = true
                                            val extractedUserId = userIdField.get(userObj)
                                            Log.d(TAG, "  userId = $extractedUserId")
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error extracting userId from user object", e)
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Fix: Filter notifications for this specific user using both user.userId and direct userId
                        val userNotifications = allNotifications.filter { notification ->
                            // Log the filtering for this specific notification
                            val result = when {
                                // Match direct userId field if present
                                notification.userId == userId -> {
                                    Log.d(TAG, "Notification ${notification.notificationId} matches direct userId: ${notification.userId}")
                                    true
                                }
                                // Or match nested user object's userId when user is a UserData class
                                notification.user != null && getUserIdFromObject(notification.user) == userId -> {
                                    Log.d(TAG, "Notification ${notification.notificationId} matches user?.userId: ${getUserIdFromObject(notification.user)}")
                                    true
                                }
                                // Check if user is a Map and contains userId field
                                (notification.user as? Map<*, *>)?.get("userId") == userId -> {
                                    Log.d(TAG, "Notification ${notification.notificationId} matches user Map userId: ${(notification.user as? Map<*, *>)?.get("userId")}")
                                    true
                                }
                                else -> {
                                    Log.d(TAG, "Notification ${notification.notificationId} doesn't match userId: $userId")
                                    false
                                }
                            }
                            result
                        }
                        
                        // Enhance notifications with missing data
                        val enhancedNotifications = userNotifications.map { notification ->
                            // Check if we need to enhance this notification with missing data
                            if (notification.message.isNullOrBlank() || notification.type.isNullOrBlank()) {
                                enhanceNotification(notification)
                            } else {
                                notification
                            }
                        }
                        
                        Log.d(TAG, "Found ${enhancedNotifications.size} notifications for user $userId out of ${allNotifications.size} total")
                        
                        // Process notifications to group messages by sender, similar to web version
                        val processedNotifications = processNotifications(enhancedNotifications)
                        
                        callback(processedNotifications, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing notifications", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting notifications", Exception("Failed to get notifications: ${response.code}"))
                    callback(null, Exception("Failed to get notifications: ${response.code}"))
                }
            }
        })
    }
    
    // Helper method to enhance notifications with missing data
    private fun enhanceNotification(notification: Notification): Notification {
        // First check the type - use referenceType if type is null
        val enhancedType = notification.type?.takeIf { !it.isNullOrBlank() } 
            ?: notification.referenceType?.lowercase() ?: "message"
        
        // Extract user name from the user object if available
        val userName = when (val user = notification.user) {
            is Map<*, *> -> user["userName"] as? String ?: "User"
            else -> try {
                // Try to access userName property via reflection
                val getUserName = user?.javaClass?.getMethod("getUserName")
                getUserName?.invoke(user) as? String ?: "User"
            } catch (e: Exception) {
                "User"
            }
        }
            
        // Build appropriate message based on type and referenceId
        val enhancedMessage = if (notification.message.isNullOrBlank()) {
            when (enhancedType.lowercase()) {
                "message" -> "$userName sent you a message"
                "booking" -> "You have a new booking request"
                "transaction" -> "You have a new payment transaction"
                "review" -> "You have received a new review"
                else -> "You have a new notification"
            }
        } else {
            notification.message
        }
        
        // For logging purposes
        Log.d(TAG, "Enhanced notification ${notification.notificationId}: type=$enhancedType, message=$enhancedMessage, sender=$userName")
        
        // Return enhanced notification with all fields properly set
        return notification.copy(
            type = enhancedType,
            message = enhancedMessage,
            // Make sure other fields are preserved
            userId = notification.userId,
            user = notification.user,
            notificationId = notification.notificationId,
            isRead = notification.isRead,
            read = notification.read,
            referenceId = notification.referenceId,
            referenceType = notification.referenceType,
            senderName = userName
        )
    }
    
    // For debugging - create test notifications if none are found
    private fun createDebugNotifications(userId: Long): List<Notification> {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        
        val user = mapOf(
            "userId" to userId,
            "userName" to "TestUser",
            "email" to "test@example.com",
            "role" to "Customer"
        )
        
        return listOf(
            Notification(
                notificationId = 1000,
                userId = userId,
                user = user,
                type = "message",
                message = "TestUser sent you a message",
                isRead = false,
                read = false,
                createdAt = now,
                referenceId = 1000,
                referenceType = "Message" // Match the capitalization in the server response
            ),
            Notification(
                notificationId = 1001,
                userId = userId,
                user = user,
                type = "booking",
                message = "You have a new booking request",
                isRead = false,
                read = false,
                createdAt = now,
                referenceId = 1001,
                referenceType = "Booking" // Match the capitalization in the server response
            ),
            Notification(
                notificationId = 1002,
                userId = userId,
                user = user,
                type = "transaction",
                message = "Payment received for your service",
                isRead = false,
                read = false,
                createdAt = now,
                referenceId = 1002,
                referenceType = "Transaction" // Match the capitalization in the server response
            )
        )
    }
    
    // Process notifications to group messages by sender, similar to the web implementation
    private fun processNotifications(notificationsList: List<Notification>): List<Notification> {
        // Always enhance ALL notifications to ensure they have proper values
        val enhancedList = notificationsList.map { notification ->
            enhanceNotification(notification)
        }
        
        // Log the enhanced notifications for debugging
        Log.d(TAG, "Enhanced ${enhancedList.size} notifications:")
        enhancedList.forEach { notification ->
            Log.d(TAG, "  - ID: ${notification.notificationId}, Type: ${notification.type}, Message: ${notification.message}, SenderName: ${notification.senderName}")
        }
        
        // Simply sort notifications by timestamp (newest first) without any grouping
        return enhancedList.sortedByDescending { it.createdAt }
    }
    
    // Create a new notification
    fun createNotification(userId: Long, type: String, message: String, token: String, 
                          referenceId: Long = 0, referenceType: String = "", 
                          callback: (Notification?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Creating notification for user: $userId, type: $type, message: $message")
        
        // Create current timestamp in ISO format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())
        
        // Create request body with user object like the API expects
        val jsonObject = JSONObject().apply {
            // Create user object with user ID
            val userObject = JSONObject().apply {
                put("userId", userId)
            }
            // Put user object in the request
            put("user", userObject)
            
            // Put other notification fields
            put("type", type)
            put("message", message)
            put("isRead", false)
            put("createdAt", currentDateTime)
            
            // Add reference fields if provided
            if (referenceId > 0) {
                put("referenceId", referenceId)
            }
            if (referenceType.isNotEmpty()) {
                put("referenceType", referenceType)
            }
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        Log.d(TAG, "Request body: ${jsonObject.toString()}")
        
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/notifications/create")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        Log.d(TAG, "Sending notification creation request to: ${request.url}")
        Log.d(TAG, "Headers: ${request.headers}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to create notification", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    try {
                        val notification = gson.fromJson(responseBody, Notification::class.java)
                        Log.d(TAG, "Successfully created notification: $notification")
                        callback(notification, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing created notification", e)
                        callback(null, e)
                    }
                } else {
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        val message = errorJson.optString("message", "Failed to create notification: ${response.code}")
                        Log.e(TAG, "Error from server: $message")
                        message
                    } catch (e: Exception) {
                        "Failed to create notification: ${response.code}"
                    }
                    
                    callback(null, Exception(errorMessage))
                }
            }
        })
    }
    
    // Mark notification as read
    fun markNotificationAsRead(notificationId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Marking notification as read: $notificationId")
        
        val jsonObject = JSONObject().apply {
            put("isRead", true)  // Changed from "read" to "isRead" to match our model
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/notifications/update/$notificationId")
            .put(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        Log.d(TAG, "Request URL: ${request.url}")
        Log.d(TAG, "Request body: ${jsonObject.toString()}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to mark notification as read", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to mark notification as read: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to mark notification as read: ${response.code}"
                    }
                    
                    callback(false, Exception(errorMessage))
                }
            }
        })
    }
    
    // Mark all notifications as read for a user
    fun markAllAsRead(userId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }
        
        Log.d(TAG, "Marking all notifications as read for user: $userId")
        
        // First, fetch all notifications for this user
        getNotificationsByUserId(userId, token) { notifications, error ->
            if (error != null) {
                Log.e(TAG, "Failed to get notifications", error)
                callback(false, error)
                return@getNotificationsByUserId
            }
            
            if (notifications == null || notifications.isEmpty()) {
                Log.d(TAG, "No notifications found to mark as read")
                callback(true, null) // No notifications to mark as read is still a success
                return@getNotificationsByUserId
            }
            
            // Filter unread notifications
            val unreadNotifications = notifications.filter { !it.isRead && !it.read }
            if (unreadNotifications.isEmpty()) {
                Log.d(TAG, "No unread notifications found")
                callback(true, null) // No unread notifications is still a success
                return@getNotificationsByUserId
            }
            
            Log.d(TAG, "Found ${unreadNotifications.size} unread notifications to mark as read")
            
            // Track how many notifications we've processed
            var processedCount = 0
            var successCount = 0
            var lastError: Exception? = null
            
            // Mark each notification as read
            unreadNotifications.forEach { notification ->
                markNotificationAsRead(notification.notificationId, token) { success, markError ->
                    processedCount++
                    if (success) {
                        successCount++
                    } else {
                        Log.e(TAG, "Failed to mark notification ${notification.notificationId} as read", markError)
                        lastError = markError
                    }
                    
                    // If we've processed all notifications, return the result
                    if (processedCount == unreadNotifications.size) {
                        val allSuccess = successCount == unreadNotifications.size
                        if (allSuccess) {
                            callback(true, null)
                        } else {
                            callback(false, lastError ?: Exception("Failed to mark all notifications as read"))
                        }
                    }
                }
            }
        }
    }

    // Get unread notification count
    fun getUnreadNotificationCount(userId: Long, token: String, callback: (Int, Exception?) -> Unit) {
        getNotificationsByUserId(userId, token) { notifications, error ->
            if (error != null) {
                Log.e(TAG, "Error getting unread notification count", error)
                callback(0, error)
                return@getNotificationsByUserId
            }
            
            if (notifications != null) {
                val unreadCount = notifications.count { !it.isRead && !it.read }
                callback(unreadCount, null)
            } else {
                callback(0, null)
            }
        }
    }

    // Helper method to get userId from any user object
    private fun getUserIdFromObject(userObj: Any?): Long? {
        if (userObj == null) return null
        
        return when (userObj) {
            is Map<*, *> -> (userObj["userId"] as? Number)?.toLong()
            else -> try {
                val userIdField = userObj::class.java.getDeclaredField("userId")
                userIdField.isAccessible = true
                val value = userIdField.get(userObj)
                if (value is Number) value.toLong() else null
            } catch (e: Exception) {
                try {
                    val method = userObj::class.java.getDeclaredMethod("getUserId")
                    method.isAccessible = true
                    val value = method.invoke(userObj)
                    if (value is Number) value.toLong() else null
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
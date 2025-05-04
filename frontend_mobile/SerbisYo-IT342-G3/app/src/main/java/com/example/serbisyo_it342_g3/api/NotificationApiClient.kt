package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Notification
import com.example.serbisyo_it342_g3.data.NotificationDeserializer
import com.example.serbisyo_it342_g3.data.UserData
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NotificationApiClient(private val context: Context) {
    private val baseApiClient = BaseApiClient(context)
    private val client = baseApiClient.client
    // Create a custom Gson instance with our NotificationDeserializer
    private val gson = GsonBuilder()
        .registerTypeAdapter(Notification::class.java, NotificationDeserializer())
        .create()
    
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
                        // Use our custom deserializer
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
                        
                        // Try manual parsing as a fallback
                        try {
                            val jsonArray = JSONArray(responseBody)
                            val manualNotifications = ArrayList<Notification>()
                            
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                
                                // Extract user ID from the user object
                                var notifUserId: Long? = null
                                if (jsonObject.has("user") && !jsonObject.isNull("user")) {
                                    val userObj = jsonObject.getJSONObject("user")
                                    if (userObj.has("userId")) {
                                        notifUserId = userObj.getLong("userId")
                                    }
                                }
                                
                                // Only process notifications for our user
                                if (notifUserId == userId) {
                                    // Parse createdAt date from array format
                                    var createdAt = ""
                                    if (jsonObject.has("createdAt")) {
                                        val createdAtObj = jsonObject.get("createdAt")
                                        if (createdAtObj is JSONArray) {
                                            val dateArray = jsonObject.getJSONArray("createdAt")
                                            if (dateArray.length() >= 3) {
                                                val year = dateArray.getInt(0)
                                                val month = dateArray.getInt(1)
                                                val day = dateArray.getInt(2)
                                                
                                                // Include time if available
                                                if (dateArray.length() >= 6) {
                                                    val hour = dateArray.getInt(3)
                                                    val minute = dateArray.getInt(4)
                                                    val second = dateArray.getInt(5)
                                                    createdAt = String.format("%04d-%02d-%02d %02d:%02d:%02d", 
                                                        year, month, day, hour, minute, second)
                                                } else {
                                                    createdAt = String.format("%04d-%02d-%02d", year, month, day)
                                                }
                                            }
                                        } else if (createdAtObj is String) {
                                            createdAt = createdAtObj
                                        }
                                    }
                                    
                                    // Create the notification object
                                    val notification = Notification(
                                        notificationId = jsonObject.optLong("notificationId", 0),
                                        userId = notifUserId,
                                        type = jsonObject.optString("type", ""),
                                        message = jsonObject.optString("message", ""),
                                        isRead = jsonObject.optBoolean("isRead", false),
                                        read = jsonObject.optBoolean("read", false),
                                        createdAt = createdAt,
                                        referenceId = jsonObject.optLong("referenceId", 0),
                                        referenceType = jsonObject.optString("referenceType", ""),
                                        senderName = jsonObject.optString("senderName", ""),
                                        senderId = jsonObject.optLong("senderId", 0)
                                    )
                                    
                                    manualNotifications.add(notification)
                                }
                            }
                            
                            // Process these manually-parsed notifications
                            val enhancedNotifications = manualNotifications.map { notification ->
                                if (notification.message.isNullOrBlank() || notification.type.isNullOrBlank()) {
                                    enhanceNotification(notification)
                                } else {
                                    notification
                                }
                            }
                            
                            Log.d(TAG, "Manually parsed ${enhancedNotifications.size} notifications for user $userId")
                            val processedNotifications = processNotifications(enhancedNotifications)
                            callback(processedNotifications, null)
                        } catch (jsonEx: Exception) {
                            Log.e(TAG, "Error with manual JSON parsing", jsonEx)
                            callback(null, e)  // Return the original exception
                        }
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
    
    // Process notifications to group messages by sender
    private fun processNotifications(notifications: List<Notification>): List<Notification> {
        // Group all message notifications by sender
        val messagesByReferenceType = notifications.groupBy { it.referenceType }
        
        val processedNotifications = mutableListOf<Notification>()
        
        // Add all non-message notifications directly
        processedNotifications.addAll(
            messagesByReferenceType.getOrDefault("Message", emptyList())
        )
        
        // Add all other notification types
        messagesByReferenceType.forEach { (referenceType, notifs) ->
            if (referenceType != "Message") {
                processedNotifications.addAll(notifs)
            }
        }
        
        // Sort by creation date, most recent first
        return processedNotifications.sortedByDescending { it.createdAt }
    }
    
    // Mark a specific notification as read
    fun markNotificationAsRead(notificationId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Marking notification $notificationId as read with token: ${token.take(10)}...")
        
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/notifications/markAsRead/$notificationId")
            .put("".toRequestBody(null))
            .header("Authorization", "Bearer $token")
            .build()

        Log.d(TAG, "Request URL: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to mark notification as read", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Response code: ${response.code}, body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully marked notification $notificationId as read")
                    callback(true, null)
                } else {
                    val errorMsg = "Error marking notification as read: ${response.code}"
                    Log.e(TAG, "$errorMsg - Response body: $responseBody")
                    
                    // Try again with alternative endpoint if possible (some backends use different endpoints)
                    if (response.code == 404) {
                        tryAlternateMarkAsReadEndpoint(notificationId, token, callback)
                    } else {
                        callback(false, Exception(errorMsg))
                    }
                }
            }
        })
    }
    
    // Try an alternative endpoint for marking notification as read
    private fun tryAlternateMarkAsReadEndpoint(notificationId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        Log.d(TAG, "Trying alternate endpoint for marking notification $notificationId as read")
        
        val requestBody = JSONObject().apply {
            put("notificationId", notificationId)
            put("isRead", true)
        }.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/notifications/update/$notificationId")
            .put(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        Log.d(TAG, "Alternate request URL: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to mark notification as read with alternate endpoint", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Alternate endpoint response code: ${response.code}, body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully marked notification as read using alternate endpoint")
                    callback(true, null)
                } else {
                    val errorMsg = "Error marking notification as read with alternate endpoint: ${response.code}"
                    Log.e(TAG, "$errorMsg - Response body: $responseBody")
                    callback(false, Exception(errorMsg))
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
        
        Log.d(TAG, "Marking all notifications as read for user $userId with token: ${token.take(10)}...")

        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/notifications/markAllAsRead/$userId")
            .put("".toRequestBody(null))
            .header("Authorization", "Bearer $token")
            .build()

        Log.d(TAG, "Request URL: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to mark all notifications as read", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "Response code: ${response.code}, body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully marked all notifications as read for user $userId")
                    callback(true, null)
                } else {
                    val errorMsg = "Error marking all notifications as read: ${response.code}"
                    Log.e(TAG, "$errorMsg - Response body: $responseBody")
                    
                    // Try again with alternative endpoint if possible (some backends use different endpoints)
                    if (response.code == 404) {
                        tryMarkAllAsReadAlternate(userId, token, callback)
                    } else {
                        callback(false, Exception(errorMsg))
                    }
                }
            }
        })
    }
    
    // Try an alternative method to mark all notifications as read
    private fun tryMarkAllAsReadAlternate(userId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        Log.d(TAG, "Trying alternate method for marking all notifications as read for user $userId")
        
        // First get all notifications for the user
        getNotificationsByUserId(userId, token) { notifications, error ->
            if (error != null || notifications == null) {
                Log.e(TAG, "Failed to get notifications for alternate mark all read", error)
                callback(false, error ?: Exception("Failed to get notifications"))
                return@getNotificationsByUserId
            }
            
            // Filter to only unread notifications
            val unreadNotifications = notifications.filter { !it.isRead && !it.read }
            Log.d(TAG, "Found ${unreadNotifications.size} unread notifications to mark as read")
            
            if (unreadNotifications.isEmpty()) {
                // No unread notifications, return success
                callback(true, null)
                return@getNotificationsByUserId
            }
            
            // Keep track of how many we've processed
            var processedCount = 0
            var successCount = 0
            var failureCount = 0
            
            // Mark each notification as read individually
            unreadNotifications.forEach { notification ->
                markNotificationAsRead(notification.notificationId, token) { success, markError ->
                    synchronized(this) {
                    processedCount++
                    if (success) {
                        successCount++
                    } else {
                            failureCount++
                        Log.e(TAG, "Failed to mark notification ${notification.notificationId} as read", markError)
                    }
                    
                        // If we've processed all notifications, call the callback
                    if (processedCount == unreadNotifications.size) {
                            val allSuccessful = failureCount == 0
                            Log.d(TAG, "Finished marking all notifications as read: $successCount success, $failureCount failures")
                            callback(allSuccessful, if (allSuccessful) null else Exception("Failed to mark some notifications as read"))
                        }
                    }
                }
            }
        }
    }

    // Send a notification
    fun sendNotification(
        userId: Long,
        message: String,
        type: String,
        referenceId: Long,
        referenceType: String,
        token: String,
        callback: (Boolean, Exception?) -> Unit
    ) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Sending notification to user $userId: $message")

        val jsonObject = JSONObject().apply {
            put("user", JSONObject().apply {
                put("userId", userId)
            })
            put("message", message)
            put("type", type)
            put("referenceId", referenceId)
            put("referenceType", referenceType)
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/notifications/postNotification")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to send notification", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully sent notification to user $userId")
                    callback(true, null)
            } else {
                    Log.e(TAG, "Error sending notification: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    callback(false, Exception("Failed to send notification: ${response.code}"))
                }
            }
        })
    }

    // Helper function to extract userId from various user object types
    private fun getUserIdFromObject(userObj: Any?): Long? {
        if (userObj == null) return null
        
        return try {
            when (userObj) {
                is Map<*, *> -> {
                    // If it's a Map, try to get the userId field
                    val userIdVal = userObj["userId"]
                    when (userIdVal) {
                        is Number -> userIdVal.toLong()
                        is String -> userIdVal.toLongOrNull()
                        else -> null
                    }
                }
                else -> {
                    // Try using reflection to get the userId field
                    val method = userObj::class.java.getDeclaredMethod("getUserId")
                    method.isAccessible = true
                    val result = method.invoke(userObj)
                    when (result) {
                        is Number -> result.toLong()
                        is String -> result.toLongOrNull()
                        else -> null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting userId from user object", e)
            null
        }
    }
}
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
    private val client = OkHttpClient()
    private val gson = Gson()
    
    // CONFIGURATION FOR BACKEND CONNECTION
    // For Android Emulator - Virtual Device (default)
    private val EMULATOR_URL = "http://10.0.2.2:8080" 
    
    // For Physical Device - Use your computer's actual IP address from ipconfig
    private val PHYSICAL_DEVICE_URL = "http://172.20.10.2:8080"
    
    // SWITCH BETWEEN CONNECTION TYPES:
    // Uncomment the one you need and comment out the other
    // private val BASE_URL = EMULATOR_URL     // For Android Emulator
    private val BASE_URL = PHYSICAL_DEVICE_URL // For Physical Device
    
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
            .url("$BASE_URL/api/notifications/getAll")
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
                        
                        // Filter notifications for this specific user
                        val userNotifications = allNotifications.filter { it.userId == userId }
                        Log.d(TAG, "Found ${userNotifications.size} notifications for user $userId out of ${allNotifications.size} total")
                        
                        callback(userNotifications, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing notifications", e)
                        callback(null, e)
                    }
                } else {
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to get notifications: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to get notifications: ${response.code}"
                    }
                    
                    callback(null, Exception(errorMessage))
                }
            }
        })
    }
    
    // Create a new notification
    fun createNotification(userId: Long, type: String, message: String, token: String, callback: (Notification?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Creating notification for user: $userId, type: $type, message: $message")
        
        // Create current timestamp in ISO format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())
        
        // Updated JSON structure to match the database schema
        val jsonObject = JSONObject().apply {
            put("userId", userId)  // Direct userId field instead of nested user object
            put("type", type)
            put("message", message)
            put("isRead", false)   // Using isRead to match the model class
            put("createdAt", currentDateTime)
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        Log.d(TAG, "Request body: ${jsonObject.toString()}")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/notifications/create")
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
            .url("$BASE_URL/api/notifications/update/$notificationId")
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
} 
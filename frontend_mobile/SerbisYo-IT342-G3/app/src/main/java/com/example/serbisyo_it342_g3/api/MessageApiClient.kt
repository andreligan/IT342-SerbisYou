package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Conversation
import com.example.serbisyo_it342_g3.data.Message
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MessageApiClient(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()
    
    // CONFIGURATION FOR BACKEND CONNECTION
    // For Android Emulator - Virtual Device (default)
    private val EMULATOR_URL = "http://10.0.2.2:8080" 
    
    // For Physical Device - Use your computer's actual IP address from ipconfig
    private val PHYSICAL_DEVICE_URL = "http://192.168.254.103:8080"
    
    // SWITCH BETWEEN CONNECTION TYPES:
    // Uncomment the one you need and comment out the other
    // private val BASE_URL = EMULATOR_URL     // For Android Emulator
    private val BASE_URL = PHYSICAL_DEVICE_URL // For Physical Device
    
    private val TAG = "MessageApiClient"

    // Get conversations for a user
    fun getConversations(userId: Long, token: String, callback: (List<Conversation>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Getting conversation partners for user: $userId")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/messages/conversation-partners/$userId")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get conversations", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        // Parse response into a list of map objects
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val conversationsData = gson.fromJson<List<Map<String, Any>>>(responseBody, type)
                        
                        // Convert the API response format to our Conversation objects
                        val conversations = conversationsData.map { data ->
                            // Extract lastMessageTime and handle it correctly
                            val lastMessageTimeStr = data["lastMessageTime"] as? String
                            val lastMessageTime = if (lastMessageTimeStr != null) {
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                    .parse(lastMessageTimeStr) ?: Date()
                            } else {
                                Date()
                            }
                            
                            // Extract the user ID
                            val userId = (data["userId"] as? Double)?.toLong().toString()
                            
                            // Create a proper name from firstName and lastName if available
                            val firstName = data["firstName"] as? String ?: ""
                            val lastName = data["lastName"] as? String ?: ""
                            val businessName = data["businessName"] as? String
                            val userName = when {
                                businessName != null -> businessName
                                firstName.isNotEmpty() && lastName.isNotEmpty() -> "$firstName $lastName"
                                else -> data["userName"] as? String ?: "Unknown"
                            }
                            
                            // Create the Conversation object
                            Conversation(
                                id = userId,
                                userId = userId.toLongOrNull() ?: 0L,
                                userName = userName,
                                userRole = data["role"] as? String ?: "Unknown",
                                profileImage = data["profileImage"] as? String,
                                lastMessage = data["lastMessage"] as? String ?: "",
                                lastMessageTime = lastMessageTime,
                                unreadCount = if (data["isUnread"] as? Boolean == true) 1 else 0
                            )
                        }
                        callback(conversations, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing conversations", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting conversations: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    val errorMessage = when (response.code) {
                        401 -> "Authentication failed: Please log in again (401 Unauthorized)"
                        403 -> "Permission denied: You don't have access to view conversations (403 Forbidden)"
                        404 -> "No conversations found for this user"
                        else -> "Failed to get conversations: ${response.code}"
                    }
                    
                    callback(null, Exception(errorMessage))
                }
            }
        })
    }

    // Get messages between two users
    fun getMessages(userId: Long, otherUserId: Long, token: String, callback: (List<Message>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Getting messages between users: $userId and $otherUserId")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/messages/conversation/$userId/$otherUserId")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get messages", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        // First parse the messages as maps
                        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                        val messagesData = gson.fromJson<List<Map<String, Any>>>(responseBody, type)
                        
                        // Convert to our Message objects
                        val messages = messagesData.map { data ->
                            // Extract the message data
                            val messageMap = data["messageId"] as? Map<String, Any>
                            val messageId = (messageMap?.get("messageId") as? Double)?.toLong()
                            
                            // Extract sender and receiver info
                            val senderMap = data["sender"] as? Map<String, Any>
                            val senderId = (senderMap?.get("userId") as? Double)?.toLong() ?: 0L
                            val senderName = senderMap?.get("userName") as? String
                            val senderRole = senderMap?.get("role") as? String
                            
                            val receiverMap = data["receiver"] as? Map<String, Any>
                            val recipientId = (receiverMap?.get("userId") as? Double)?.toLong() ?: 0L
                            
                            // Extract message text and time
                            val content = data["messageText"] as? String ?: ""
                            val sentAtStr = data["sentAt"] as? String
                            val timestamp = if (sentAtStr != null) {
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                                    .parse(sentAtStr) ?: Date()
                            } else {
                                Date()
                            }
                            
                            // Extract status and convert to read boolean
                            val status = data["status"] as? String
                            val read = status == "READ"
                            
                            Message(
                                messageId = messageId,
                                senderId = senderId,
                                recipientId = recipientId,
                                content = content,
                                timestamp = timestamp,
                                read = read,
                                senderName = senderName,
                                senderRole = senderRole
                            )
                        }
                        callback(messages, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing messages", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting messages: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    val errorMessage = when (response.code) {
                        401 -> "Authentication failed: Please log in again (401 Unauthorized)"
                        403 -> "Permission denied: You don't have access to these messages (403 Forbidden)"
                        404 -> "No messages found between these users"
                        else -> "Failed to get messages: ${response.code}"
                    }
                    
                    callback(null, Exception(errorMessage))
                }
            }
        })
    }

    // Send a message
    fun sendMessage(message: Message, token: String, callback: (Message?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Sending message from ${message.senderId} to ${message.recipientId}")
        
        val jsonObject = JSONObject().apply {
            // Create a nested sender JSON object
            val senderObject = JSONObject().apply {
                put("userId", message.senderId)
            }
            put("sender", senderObject)
            
            // Create a nested receiver JSON object
            val receiverObject = JSONObject().apply {
                put("userId", message.recipientId)
            }
            put("receiver", receiverObject)
            
            // Add message text
            put("messageText", message.content)
            
            // Format the date in ISO format
            put("sentAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(message.timestamp))
            
            // Set the status (READ/UNREAD)
            put("status", "UNREAD")
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url("$BASE_URL/api/messages/postMessage")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to send message", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        // Parse the response into a map first
                        val messageData = gson.fromJson(responseBody, Map::class.java)
                        
                        // Extract the necessary data
                        val messageId = (messageData["messageId"] as? Double)?.toLong()
                        
                        // Create a new Message object with the server-assigned ID
                        val sentMessage = message.copy(messageId = messageId)
                        callback(sentMessage, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing sent message", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error sending message: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    val errorMessage = when (response.code) {
                        401 -> "Authentication failed: Please log in again (401 Unauthorized)"
                        403 -> "Permission denied: You don't have access to send messages (403 Forbidden)"
                        else -> "Failed to send message: ${response.code}"
                    }
                    
                    callback(null, Exception(errorMessage))
                }
            }
        })
    }

    // Mark messages as read
    fun markMessagesAsRead(userId: Long, senderId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Marking messages as read from $senderId to $userId")
        
        val jsonObject = JSONObject().apply {
            // Create the update object with sender and receiver IDs
            val receiverObject = JSONObject().apply {
                put("userId", userId)
            }
            put("receiver", receiverObject)
            
            val senderObject = JSONObject().apply {
                put("userId", senderId)
            }
            put("sender", senderObject)
            
            // Set the new status
            put("status", "READ")
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        // Update message with ID to mark all messages in a conversation as read
        val request = Request.Builder()
            .url("$BASE_URL/api/messages/updateMessage/status")
            .put(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to mark messages as read", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    Log.e(TAG, "Error marking messages as read: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    val errorMessage = when (response.code) {
                        401 -> "Authentication failed: Please log in again (401 Unauthorized)"
                        403 -> "Permission denied: You don't have access to mark these messages (403 Forbidden)"
                        else -> "Failed to mark messages as read: ${response.code}"
                    }
                    
                    callback(false, Exception(errorMessage))
                }
            }
        })
    }
} 
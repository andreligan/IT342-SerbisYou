package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Conversation
import com.example.serbisyo_it342_g3.data.Message
import com.example.serbisyo_it342_g3.data.UserSearchModel
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
    private val PHYSICAL_DEVICE_URL = "http://192.168.254.116:8080"

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

    // Search for users by name or username
    fun searchUsers(query: String, token: String, callback: (List<UserSearchModel>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Searching for users with query: $query")

        // Instead of using a search endpoint that doesn't exist, fetch all users and filter locally
        // We'll need to fetch both customers and service providers
        fetchAllUsers(token) { users, error ->
            if (error != null) {
                Log.e(TAG, "Failed to fetch users for search", error)
                callback(null, error)
                return@fetchAllUsers
            }

            Log.d(TAG, "Total users fetched before filtering: ${users?.size ?: 0}")

            if (users != null && users.isNotEmpty()) {
                // Filter users locally based on query
                val lowercaseQuery = query.lowercase()
                val filteredUsers = users.filter { user ->
                    Log.d(TAG, "Checking user: ${user.userName} (${user.firstName} ${user.lastName}) role: ${user.role}")

                    val matches = user.userName.lowercase().contains(lowercaseQuery) ||
                            user.firstName?.lowercase()?.contains(lowercaseQuery) == true ||
                            user.lastName?.lowercase()?.contains(lowercaseQuery) == true ||
                            user.businessName?.lowercase()?.contains(lowercaseQuery) == true ||
                            user.email?.lowercase()?.contains(lowercaseQuery) == true

                    if (matches) {
                        Log.d(TAG, "User MATCHED query: $user")
                    }
                    matches
                }

                Log.d(TAG, "Found ${filteredUsers.size} users matching '$query'")
                callback(filteredUsers, null)
            } else {
                Log.d(TAG, "No users found to filter")
                callback(emptyList(), null)
            }
        }
    }

    // New method to fetch all users
    private fun fetchAllUsers(token: String, callback: (List<UserSearchModel>?, Exception?) -> Unit) {
        // Create requests for both customers and service providers
        val customersRequest = Request.Builder()
            .url("$BASE_URL/api/customers/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        val providersRequest = Request.Builder()
            .url("$BASE_URL/api/service-providers/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        // Execute request for customers
        client.newCall(customersRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch customers", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error fetching customers: ${response.code}")
                    callback(null, Exception("Failed to fetch customers: ${response.code}"))
                    return
                }

                val responseBody = response.body?.string()
                if (responseBody == null) {
                    callback(null, Exception("Empty response when fetching customers"))
                    return
                }

                Log.d(TAG, "Customer response received, length: ${responseBody.length}")

                try {
                    // Parse customers
                    val customerList = gson.fromJson<List<Map<String, Any>>>(
                        responseBody,
                        object : TypeToken<List<Map<String, Any>>>() {}.type
                    )

                    Log.d(TAG, "Parsed ${customerList.size} customers")

                    // Now fetch service providers
                    client.newCall(providersRequest).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.e(TAG, "Failed to fetch service providers", e)

                            // Return only customer results
                            val customers = parseCustomers(customerList)
                            Log.d(TAG, "Returning ${customers.size} customers without providers due to error")
                            callback(customers, null)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val customers = parseCustomers(customerList)
                            Log.d(TAG, "Successfully parsed ${customers.size} customers")

                            if (!response.isSuccessful) {
                                Log.e(TAG, "Error fetching service providers: ${response.code}")
                                // Return only customer results
                                callback(customers, null)
                                return
                            }

                            val providerResponseBody = response.body?.string()
                            if (providerResponseBody == null) {
                                // Return only customer results
                                callback(customers, null)
                                return
                            }

                            Log.d(TAG, "Provider response received, length: ${providerResponseBody.length}")

                            try {
                                // Parse service providers
                                val providerList = gson.fromJson<List<Map<String, Any>>>(
                                    providerResponseBody,
                                    object : TypeToken<List<Map<String, Any>>>() {}.type
                                )

                                Log.d(TAG, "Parsed ${providerList.size} service providers")

                                // Combine customers and providers
                                val providers = parseProviders(providerList)
                                Log.d(TAG, "Successfully parsed ${providers.size} providers")

                                val allUsers = customers + providers
                                Log.d(TAG, "Combined total: ${allUsers.size} users")

                                callback(allUsers, null)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing service providers", e)
                                // Return only customer results
                                callback(customers, null)
                            }
                        }
                    })
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing customers", e)
                    callback(null, e)
                }
            }
        })
    }

    private fun parseCustomers(customerList: List<Map<String, Any>>): List<UserSearchModel> {
        val results = mutableListOf<UserSearchModel>()

        for (customerData in customerList) {
            try {
                // Try to get the userId from different locations
                val userAuth = customerData["userAuth"] as? Map<String, Any>
                val userId = userAuth?.get("userId") as? Double
                    ?: customerData["customerId"] as? Double
                    ?: continue

                val userName = userAuth?.get("userName") as? String ?: ""
                val email = userAuth?.get("email") as? String ?: ""

                val firstName = customerData["firstName"] as? String ?: ""
                val lastName = customerData["lastName"] as? String ?: ""
                val profileImage = customerData["profileImage"] as? String

                val user = UserSearchModel(
                    userId = userId.toLong().toString(),
                    userName = userName,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    role = "Customer",
                    profileImage = profileImage,
                    businessName = null
                )

                Log.d(TAG, "Parsed customer: $userName (ID: $userId)")
                results.add(user)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing individual customer data", e)
                // Continue with next customer
            }
        }

        return results
    }

    private fun parseProviders(providerList: List<Map<String, Any>>): List<UserSearchModel> {
        val results = mutableListOf<UserSearchModel>()

        for (providerData in providerList) {
            try {
                // Try to get the userId from different locations
                val userAuth = providerData["userAuth"] as? Map<String, Any>
                val userId = userAuth?.get("userId") as? Double
                    ?: providerData["providerId"] as? Double
                    ?: continue

                val userName = userAuth?.get("userName") as? String ?: ""
                val email = userAuth?.get("email") as? String ?: ""

                val firstName = providerData["firstName"] as? String ?: ""
                val lastName = providerData["lastName"] as? String ?: ""
                val businessName = providerData["businessName"] as? String
                val profileImage = providerData["profileImage"] as? String

                val user = UserSearchModel(
                    userId = userId.toLong().toString(),
                    userName = userName,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    role = "Service Provider",
                    profileImage = profileImage,
                    businessName = businessName
                )

                Log.d(TAG, "Parsed provider: $userName / $businessName (ID: $userId)")
                results.add(user)
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing individual provider data", e)
                // Continue with next provider
            }
        }

        return results
    }
}
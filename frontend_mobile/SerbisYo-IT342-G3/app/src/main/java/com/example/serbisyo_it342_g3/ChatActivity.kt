package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.MessageAdapter
import com.example.serbisyo_it342_g3.api.MessageApiClient
import com.example.serbisyo_it342_g3.data.Message
import com.example.serbisyo_it342_g3.data.MessageStatus
import com.example.serbisyo_it342_g3.utils.SharedPreferencesUtil
import java.util.Date
import android.net.Uri
import java.io.File

class ChatActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var ivUserProfile: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserStatus: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var rvMessages: RecyclerView
    private lateinit var tvNoMessages: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    
    private lateinit var messageApiClient: MessageApiClient
    private var currentUserId: Long = 0
    private var otherUserId: Long = 0
    private var otherUserName: String = ""
    private var otherUserRole: String = ""
    private var otherUserProfileImage: String? = null
    private var token: String = ""
    
    private val TAG = "ChatActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        // Initialize MessageApiClient
        messageApiClient = MessageApiClient(this)
        
        // Get data from intent with better error handling
        try {
            otherUserId = intent.getLongExtra("USER_ID", 0)
            otherUserName = intent.getStringExtra("USER_NAME") ?: "User"
            otherUserRole = intent.getStringExtra("USER_ROLE") ?: ""
            otherUserProfileImage = intent.getStringExtra("PROFILE_IMAGE")
            
            Log.d(TAG, "Received intent data - UserId: $otherUserId, Name: $otherUserName, Role: $otherUserRole")
            
            // Validate user ID
            if (otherUserId <= 0) {
                Log.e(TAG, "Invalid other user ID: $otherUserId")
                Toast.makeText(this, "Invalid user information", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting intent data", e)
            Toast.makeText(this, "Error loading chat information", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Get current user data from SharedPreferences directly
        try {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            token = sharedPref.getString("token", "") ?: ""
            
            // Fix userId retrieval using try-catch
            currentUserId = try {
                // Try to get as Long first (new format)
                sharedPref.getLong("userId", 0)
            } catch (e: ClassCastException) {
                // If that fails, try the String format (old format) and convert
                val userIdStr = sharedPref.getString("userId", "0")
                userIdStr?.toLongOrNull() ?: 0
            }
            
            Log.d(TAG, "Current user ID: $currentUserId, Token length: ${token.length}")
            
            if (currentUserId <= 0 || token.isEmpty()) {
                Log.e(TAG, "Invalid credentials - currentUserId: $currentUserId, token empty: ${token.isEmpty()}")
                Toast.makeText(this, "Please log in again to access chat", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user credentials", e)
            Toast.makeText(this, "Error accessing user credentials", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        Log.d(TAG, "Chat between users: current=$currentUserId, other=$otherUserId")
        
        // Initialize views
        initViews()
        
        // Set up the toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // Set up the chat header
        tvUserName.text = otherUserName
        tvUserStatus.text = otherUserRole
        
        // Load user profile image
        if (!otherUserProfileImage.isNullOrEmpty()) {
            try {
                loadProfileImage(ivUserProfile, otherUserProfileImage)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling profile image", e)
            }
        }
        
        // Set up the back button
        btnBack.setOnClickListener {
            finish()
        }
        
        // Set up the RecyclerView
        setupRecyclerView()
        
        // Set up the send button
        btnSend.setOnClickListener {
            val messageContent = etMessage.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                sendMessage(messageContent)
                etMessage.text.clear()
            }
        }
        
        // Load messages
        loadMessages()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        ivUserProfile = findViewById(R.id.ivUserProfile)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserStatus = findViewById(R.id.tvUserStatus)
        btnBack = findViewById(R.id.btnBack)
        rvMessages = findViewById(R.id.rvMessages)
        tvNoMessages = findViewById(R.id.tvNoMessages)
        progressBar = findViewById(R.id.progressBar)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(this, messages, currentUserId)
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Show newest messages at the bottom
        }
        rvMessages.adapter = messageAdapter
    }
    
    private fun loadMessages() {
        progressBar.visibility = View.VISIBLE
        rvMessages.visibility = View.GONE
        tvNoMessages.visibility = View.GONE
        
        // Load from database first if available (future implementation)
        // For now, just show loading and fetch from API
        
        Log.d(TAG, "Loading messages between users $currentUserId and $otherUserId")
        
        try {
            messageApiClient.getMessages(currentUserId, otherUserId, token) { messagesList, error ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    if (error != null) {
                        Log.e(TAG, "Error loading messages", error)
                        tvNoMessages.text = "Error loading messages"
                        tvNoMessages.visibility = View.VISIBLE
                        
                        // If there's a connection error, show a specific message
                        if (error.message?.contains("timeout", ignoreCase = true) == true ||
                            error.message?.contains("failed to connect", ignoreCase = true) == true) {
                            Toast.makeText(this, "Connection error. Please check your network.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to load messages: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                        
                        // If we already have messages loaded, keep showing them
                        if (messages.isNotEmpty()) {
                            rvMessages.visibility = View.VISIBLE
                            tvNoMessages.visibility = View.GONE
                        }
                        
                        return@runOnUiThread
                    }
                    
                    if (messagesList != null && messagesList.isNotEmpty()) {
                        messages.clear()
                        messages.addAll(messagesList)
                        messageAdapter.notifyDataSetChanged()
                        rvMessages.visibility = View.VISIBLE
                        tvNoMessages.visibility = View.GONE
                        
                        // Scroll to the bottom
                        rvMessages.scrollToPosition(messages.size - 1)
                        
                        // Mark messages as read
                        markMessagesAsRead()
                    } else {
                        // No messages found
                        if (messages.isEmpty()) {
                            tvNoMessages.text = "No messages yet. Start a conversation!"
                            tvNoMessages.visibility = View.VISIBLE
                            rvMessages.visibility = View.GONE
                        } else {
                            // Keep showing existing messages if we have them
                            rvMessages.visibility = View.VISIBLE
                            tvNoMessages.visibility = View.GONE
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling getMessages", e)
            runOnUiThread {
                progressBar.visibility = View.GONE
                tvNoMessages.text = "Error: ${e.message}"
                tvNoMessages.visibility = View.VISIBLE
            }
        }
    }
    
    private fun sendMessage(content: String) {
        val message = Message(
            messageId = null,
            senderId = currentUserId,
            recipientId = otherUserId,
            content = content,
            timestamp = Date(),
            read = false,
            status = MessageStatus.SENDING,
            senderName = null,
            senderRole = null
        )
        
        // Optimistically add the message to the UI
        messages.add(message)
        messageAdapter.notifyItemInserted(messages.size - 1)
        rvMessages.scrollToPosition(messages.size - 1)
        
        // Make the RecyclerView visible if it was hidden
        if (rvMessages.visibility != View.VISIBLE) {
            tvNoMessages.visibility = View.GONE
            rvMessages.visibility = View.VISIBLE
        }
        
        // Send to server
        try {
            messageApiClient.sendMessage(message, token) { sentMessage, error ->
                runOnUiThread {
                    if (error != null) {
                        Log.e(TAG, "Error sending message", error)
                        
                        // Add a visual indicator that message failed to send
                        Toast.makeText(
                            this,
                            "Failed to send message",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Mark the message as failed in the UI
                        val lastIndex = messages.size - 1
                        if (lastIndex >= 0) {
                            // Update the message status to ERROR
                            messages[lastIndex] = messages[lastIndex].copy(status = MessageStatus.ERROR)
                            messageAdapter.notifyItemChanged(lastIndex)
                        }
                        
                        return@runOnUiThread
                    }
                    
                    if (sentMessage != null) {
                        // Replace the optimistic message with the real one from the server
                        val lastIndex = messages.size - 1
                        if (lastIndex >= 0) {
                            messages[lastIndex] = sentMessage.copy(status = MessageStatus.SENT)
                            messageAdapter.notifyItemChanged(lastIndex)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling sendMessage", e)
            runOnUiThread {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                
                // Update the message status to ERROR
                val lastIndex = messages.size - 1
                if (lastIndex >= 0) {
                    messages[lastIndex] = messages[lastIndex].copy(status = MessageStatus.ERROR)
                    messageAdapter.notifyItemChanged(lastIndex)
                }
            }
        }
    }
    
    private fun markMessagesAsRead() {
        // Check if there are any unread messages from the other user
        val hasUnreadMessages = messages.any { it.senderId == otherUserId && !it.read }
        
        if (!hasUnreadMessages) {
            Log.d(TAG, "No unread messages to mark as read")
            return
        }
        
        try {
            messageApiClient.markMessagesAsRead(currentUserId, otherUserId, token) { success, error ->
                if (error != null) {
                    Log.e(TAG, "Error marking messages as read", error)
                    return@markMessagesAsRead
                }
                
                if (success) {
                    Log.d(TAG, "Messages marked as read successfully")
                    
                    // Update UI to show messages as read
                    runOnUiThread {
                        var changed = false
                        for (i in messages.indices) {
                            if (messages[i].senderId == otherUserId && !messages[i].read) {
                                messages[i] = messages[i].copy(read = true)
                                changed = true
                            }
                        }
                        
                        // Update the adapter efficiently if any messages changed
                        if (changed) {
                            messageAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling markMessagesAsRead", e)
        }
    }
    
    private fun loadProfileImage(imageView: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_profile)
            return
        }
        
        try {
            // Try to load from local file if the path is a file path
            val file = File(imageUrl)
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file))
                return
            }
            
            // If it's not a local file, we would need a proper image loading library
            // like Glide or Picasso. For now, just use the default image.
            Log.d(TAG, "Would load image from URL: $imageUrl (using default image instead)")
            imageView.setImageResource(R.drawable.ic_profile)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image", e)
            imageView.setImageResource(R.drawable.ic_profile)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh messages when activity resumes
        if (::messageApiClient.isInitialized && currentUserId > 0 && otherUserId > 0) {
            loadMessages()
        }
    }
}
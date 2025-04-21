package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.serbisyo_it342_g3.adapters.MessageAdapter
import com.example.serbisyo_it342_g3.api.MessageApiClient
import com.example.serbisyo_it342_g3.data.Message
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Date

class ChatActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var ivUserProfile: CircleImageView
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
        
        // Get data from intent
        otherUserId = intent.getLongExtra("USER_ID", 0)
        otherUserName = intent.getStringExtra("USER_NAME") ?: "User"
        otherUserRole = intent.getStringExtra("USER_ROLE") ?: ""
        otherUserProfileImage = intent.getStringExtra("PROFILE_IMAGE")
        
        // Get current user data from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        currentUserId = sharedPref.getString("userId", "0")?.toLongOrNull() ?: 0
        token = sharedPref.getString("token", "") ?: ""
        
        if (currentUserId == 0L || otherUserId == 0L) {
            Log.e(TAG, "Invalid user IDs: currentUserId=$currentUserId, otherUserId=$otherUserId")
            finish()
            return
        }
        
        // Initialize views
        initViews()
        
        // Set up the toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // Set up the chat header
        tvUserName.text = otherUserName
        tvUserStatus.text = otherUserRole
        
        // Load user profile image
        if (otherUserProfileImage != null) {
            Glide.with(this)
                .load(otherUserProfileImage)
                .apply(RequestOptions().centerCrop().placeholder(R.drawable.ic_profile))
                .into(ivUserProfile)
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
        
        messageApiClient.getMessages(currentUserId, otherUserId, token) { messagesList, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading messages", error)
                    tvNoMessages.text = "Error loading messages: ${error.message}"
                    tvNoMessages.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                
                if (messagesList != null && messagesList.isNotEmpty()) {
                    messages.clear()
                    messages.addAll(messagesList)
                    messageAdapter.notifyDataSetChanged()
                    rvMessages.visibility = View.VISIBLE
                    
                    // Scroll to the bottom
                    rvMessages.scrollToPosition(messages.size - 1)
                    
                    // Mark messages as read
                    markMessagesAsRead()
                } else {
                    tvNoMessages.visibility = View.VISIBLE
                }
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
            read = false
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
        messageApiClient.sendMessage(message, token) { sentMessage, error ->
            if (error != null) {
                Log.e(TAG, "Error sending message", error)
                runOnUiThread {
                    // Add a visual indicator that message failed to send
                    // This would be better with a custom UI element in the MessageAdapter
                    // For now, we'll just show a toast
                    android.widget.Toast.makeText(
                        this,
                        "Failed to send message: ${error.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                return@sendMessage
            }
            
            if (sentMessage != null) {
                // Replace the optimistic message with the real one from the server
                runOnUiThread {
                    val lastIndex = messages.size - 1
                    messages[lastIndex] = sentMessage
                    messageAdapter.notifyItemChanged(lastIndex)
                }
            }
        }
    }
    
    private fun markMessagesAsRead() {
        messageApiClient.markMessagesAsRead(currentUserId, otherUserId, token) { success, error ->
            if (error != null) {
                Log.e(TAG, "Error marking messages as read", error)
                return@markMessagesAsRead
            }
            
            if (success) {
                Log.d(TAG, "Messages marked as read successfully")
                // Update UI to show messages as read if needed
                runOnUiThread {
                    for (i in messages.indices) {
                        if (messages[i].senderId == otherUserId && !messages[i].read) {
                            messages[i] = messages[i].copy(read = true)
                            messageAdapter.notifyItemChanged(i)
                        }
                    }
                }
            }
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
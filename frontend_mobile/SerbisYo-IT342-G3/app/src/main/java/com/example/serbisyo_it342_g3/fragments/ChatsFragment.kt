package com.example.serbisyo_it342_g3.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.ChatActivity
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.adapters.ConversationAdapter
import com.example.serbisyo_it342_g3.api.MessageApiClient
import com.example.serbisyo_it342_g3.data.Conversation
import java.util.Date

class ChatsFragment : Fragment() {

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoChats: TextView
    private val conversations = mutableListOf<Conversation>()
    
    private lateinit var messageApiClient: MessageApiClient
    private var userId: Long = 0
    private var token: String = ""
    
    private val TAG = "ChatsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        
        // Initialize MessageApiClient
        messageApiClient = MessageApiClient(requireContext())
        
        // Get user ID and token from SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        userId = sharedPref.getString("userId", "0")?.toLongOrNull() ?: 0
        token = sharedPref.getString("token", "") ?: ""
        
        if (userId == 0L) {
            Log.e(TAG, "User ID is invalid: $userId")
            Toast.makeText(context, "User authentication error", Toast.LENGTH_SHORT).show()
        }
        
        recyclerView = view.findViewById(R.id.rvConversations)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoChats = view.findViewById(R.id.tvNoChats)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Initialize the adapter
        conversationAdapter = ConversationAdapter(
            requireContext(),
            conversations,
            object : ConversationAdapter.OnConversationClickListener {
                override fun onConversationClick(conversation: Conversation) {
                    // Navigate to chat detail activity
                    val intent = Intent(activity, ChatActivity::class.java)
                    intent.putExtra("USER_ID", conversation.userId)
                    intent.putExtra("USER_NAME", conversation.userName)
                    intent.putExtra("USER_ROLE", conversation.userRole)
                    intent.putExtra("PROFILE_IMAGE", conversation.profileImage)
                    startActivity(intent)
                }
            }
        )
        
        recyclerView.adapter = conversationAdapter
        
        // Load conversations
        loadConversations()
        
        return view
    }
    
    private fun loadConversations() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoChats.visibility = View.GONE
        
        // If user is not authenticated, show sample data
        if (userId == 0L || token.isBlank()) {
            Log.w(TAG, "User not properly authenticated, showing sample data")
            loadSampleConversations()
            return
        }
        
        messageApiClient.getConversations(userId, token) { conversationList, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading conversations", error)
                    tvNoChats.text = "Error loading conversations"
                    tvNoChats.visibility = View.VISIBLE
                    
                    // If API call fails, fallback to sample data
                    loadSampleConversations()
                    return@runOnUiThread
                }
                
                if (conversationList != null && conversationList.isNotEmpty()) {
                    conversations.clear()
                    conversations.addAll(conversationList)
                    conversationAdapter.notifyDataSetChanged()
                    recyclerView.visibility = View.VISIBLE
                } else {
                    tvNoChats.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun loadSampleConversations() {
        // Sample data - only used when API fails or user isn't authenticated
        val sampleConversations = listOf(
            Conversation(
                id = "1",
                userId = 1L,
                userName = "John Doe",
                userRole = "Provider",
                profileImage = null,
                lastMessage = "Hello, I'd like to inquire about your services",
                lastMessageTime = Date(),
                unreadCount = 2
            ),
            Conversation(
                id = "2",
                userId = 2L,
                userName = "Maria Santos",
                userRole = "Customer",
                profileImage = null,
                lastMessage = "Thank you for the quick response!",
                lastMessageTime = Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                unreadCount = 0
            ),
            Conversation(
                id = "3",
                userId = 3L,
                userName = "Alex Johnson",
                userRole = "Provider",
                profileImage = null,
                lastMessage = "The service has been completed",
                lastMessageTime = Date(System.currentTimeMillis() - 86400000), // 1 day ago
                unreadCount = 0
            )
        )
        
        conversations.clear()
        conversations.addAll(sampleConversations)
        conversationAdapter.notifyDataSetChanged()
        
        // Show the recycler view and hide progress
        progressBar.visibility = View.GONE
        
        if (conversations.isEmpty()) {
            tvNoChats.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvNoChats.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh conversations when fragment resumes
        if (::messageApiClient.isInitialized && userId > 0) {
            loadConversations()
        }
    }
    
    companion object {
        fun newInstance(): ChatsFragment {
            return ChatsFragment()
        }
    }
} 
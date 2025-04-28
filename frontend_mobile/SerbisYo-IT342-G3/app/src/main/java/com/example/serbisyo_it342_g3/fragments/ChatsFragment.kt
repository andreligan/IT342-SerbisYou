package com.example.serbisyo_it342_g3.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.ChatActivity
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.adapters.ConversationAdapter
import com.example.serbisyo_it342_g3.adapters.UserSearchAdapter
import com.example.serbisyo_it342_g3.api.MessageApiClient
import com.example.serbisyo_it342_g3.data.Conversation
import com.example.serbisyo_it342_g3.data.UserSearchModel
import com.example.serbisyo_it342_g3.utils.CustomSwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment for displaying chat conversations
 */
class ChatsFragment : Fragment() {
    // Properties
    private val TAG = "ChatsFragment"
    private val conversations = mutableListOf<Conversation>()
    
    // User credentials
    private var userId: Long = 0
    private var token: String = ""
    
    // UI components - declared as lateinit but initialized in onViewCreated
    private lateinit var recyclerView: RecyclerView
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var fabNewMessage: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoChats: TextView
    private lateinit var messageApiClient: MessageApiClient


    companion object {
        /**
         * Creates a new instance of ChatsFragment
         * @return A new instance of ChatsFragment
         */
        @JvmStatic
        fun newInstance(): ChatsFragment {
            return ChatsFragment()
        }
    }
    
    // Simple inflation of the layout
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }
    
    // All initialization happens here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvConversations)
        fabNewMessage = view.findViewById(R.id.fabNewMessage)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoChats = view.findViewById(R.id.tvNoChats)

        // Initialize views

        
        // Get user credentials directly from SharedPreferences
        getUserCredentials()
        
        // Initialize API client
        messageApiClient = MessageApiClient(requireContext())
        
        // Setup components
        setupRecyclerView()
        setupFabNewMessage()

        
        // Load data
        loadConversations()
    }
    

    
    private fun getUserCredentials() {
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        token = sharedPref.getString("token", "") ?: ""
        // Fix userId retrieval using try-catch
        userId = try {
            // Try to get as Long first (new format)
            sharedPref.getLong("userId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            val userIdStr = sharedPref.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0L
        }
        
        Log.d(TAG, "Retrieved credentials - UserID: $userId, Token: ${token.take(15)}...")
    }
    
    // Call this when returning to the fragment
    override fun onResume() {
        super.onResume()
        if (::messageApiClient.isInitialized) {

            // Refresh credentials in case they've changed
            getUserCredentials()
            loadConversations()

        }
    }
    
    // Setup RecyclerView and its adapter
    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(
            requireContext(),
            conversations,
            object : ConversationAdapter.OnConversationClickListener {
                override fun onConversationClick(conversation: Conversation) {
                    openChatActivity(conversation)
                }
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = conversationAdapter
        }
    }
    
    // Setup FAB for new message
    private fun setupFabNewMessage() {
        fabNewMessage.setOnClickListener {
            showSearchUserDialog()
        }
    }

    
    // Load conversations
    private fun loadConversations() {
        if (!isAdded) return
        
        showLoading(true)
        
        if (userId == 0L || token.isEmpty()) {
            showError("User credentials not found. Please log in again.")
            showLoading(false)
            return
        }
        
        try {
            messageApiClient.getConversations(userId, token) { conversationList, error ->
                if (!isAdded) return@getConversations

                activity?.runOnUiThread {
                    if (error != null) {
                        Log.e(TAG, "Error loading conversations", error)
                        showError("Failed to load chats: ${error.message}")
                        showNoChats(true)
                    } else if (conversationList != null) {
                        conversations.clear()
                        conversations.addAll(conversationList)
                        conversationAdapter.notifyDataSetChanged()
                        showNoChats(conversations.isEmpty())
                    } else {
                        showNoChats(true)
                    }

                    showLoading(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading conversations", e)
            showError("Error: ${e.message}")
            showLoading(false)
        }
    }
    
    // Search for users
    private fun searchUsers(query: String, recyclerView: RecyclerView, progressBar: ProgressBar, tvNoResults: TextView) {
        if (!isAdded) return
        
        progressBar.visibility = View.VISIBLE
        tvNoResults.visibility = View.GONE
        recyclerView.visibility = View.GONE  // Hide RecyclerView initially
        
        // Check if we have credentials before proceeding
        if (token.isEmpty()) {
            // Try refreshing credentials
            getUserCredentials()
            
            if (token.isEmpty()) {
                Toast.makeText(requireContext(), "Please log in again", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                tvNoResults.visibility = View.VISIBLE
                tvNoResults.text = "Authentication error. Please log in again."
                return
            }
        }
        
        // Show debug toast
        showError("Searching for '$query'...")
        
        try {
            messageApiClient.searchUsers(query, token) { users, error -> 
                if (!isAdded) return@searchUsers
                
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    if (error != null) {
                        Log.e(TAG, "Search error: ${error.message}", error)
                        tvNoResults.visibility = View.VISIBLE
                        tvNoResults.text = "Error searching: ${error.message}"
                        return@runOnUiThread
                    }
                    
                    if (users != null && users.isNotEmpty()) {
                        Log.d(TAG, "Search found ${users.size} users: ${users.map { it.userName }}")
                        setupUserSearchAdapter(users, recyclerView)
                        recyclerView.visibility = View.VISIBLE
                        tvNoResults.visibility = View.GONE
                    } else {
                        Log.d(TAG, "Search found no users for query: $query")
                        recyclerView.visibility = View.GONE
                        tvNoResults.visibility = View.VISIBLE
                        tvNoResults.text = "No users found matching '$query'"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in searchUsers", e)
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
            tvNoResults.visibility = View.VISIBLE
            tvNoResults.text = "Error: ${e.message}"
        }
    }
    
    // Setup user search adapter
    private fun setupUserSearchAdapter(users: List<UserSearchModel>, recyclerView: RecyclerView) {
        if (!isAdded) return
        
        try {
            Log.d(TAG, "Setting up UserSearchAdapter with ${users.size} users")
            
            val userSearchAdapter = UserSearchAdapter(
                users,
                object : UserSearchAdapter.OnUserClickListener {
                    override fun onUserClick(user: UserSearchModel) {
                        try {
                            Log.d(TAG, "User clicked: ${user.userName} (ID: ${user.userId})")
                            
                            // Convert userId to Long safely
                            val userId = try {
                                user.userId.toLong()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error converting userId to Long: ${user.userId}", e)
                                0L
                            }
                            
                            // Check if userId is valid
                            if (userId <= 0) {
                                Log.e(TAG, "Invalid userId: $userId")
                                showError("Invalid user ID")
                                return
                            }
                            
                            // Create intent
                            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                                putExtra("USER_ID", userId)
                                putExtra("USER_NAME", when {
                                    !user.businessName.isNullOrEmpty() -> user.businessName
                                    !user.firstName.isNullOrEmpty() && !user.lastName.isNullOrEmpty() -> "${user.firstName} ${user.lastName}"
                                    else -> user.userName
                                })
                                putExtra("USER_ROLE", user.role)
                                putExtra("PROFILE_IMAGE", user.profileImage ?: "")
                            }
                            
                            Log.d(TAG, "Starting ChatActivity with USER_ID=$userId")
                            startActivity(intent)
                            
                            // Find and dismiss the dialog
                            dismissParentDialog(recyclerView)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error on user click", e)
                            showError("Error opening chat: ${e.message}")
                        }
                    }
                }
            )
            
            recyclerView.adapter = userSearchAdapter
            
            // Make sure the adapter has the latest data
            userSearchAdapter.notifyDataSetChanged()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up UserSearchAdapter", e)
            showError("Error displaying search results")
        }
    }
    
    // Show search user dialog
    private fun showSearchUserDialog() {
        if (!isAdded) return
        
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_search_user, null)
            val etSearch = dialogView.findViewById<EditText>(R.id.etSearch)
            val btnSearch = dialogView.findViewById<Button>(R.id.btnSearch)
            val rvUsers = dialogView.findViewById<RecyclerView>(R.id.rvUsers)
            val progressSearch = dialogView.findViewById<ProgressBar>(R.id.progressSearch)
            val tvNoResults = dialogView.findViewById<TextView>(R.id.tvNoResults)
            
            // Set up RecyclerView first
            rvUsers.layoutManager = LinearLayoutManager(requireContext())
            rvUsers.visibility = View.GONE
            
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Search User")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create()
            
            btnSearch.setOnClickListener {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    Log.d(TAG, "Search button clicked with query: $query")
                    searchUsers(query, rvUsers, progressSearch, tvNoResults)
                } else {
                    Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Also trigger search when user presses the "Enter" key
            etSearch.setOnEditorActionListener { _, actionId, event ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    val query = etSearch.text.toString().trim()
                    if (query.isNotEmpty()) {
                        Log.d(TAG, "Search triggered by IME_ACTION_SEARCH with query: $query")
                        searchUsers(query, rvUsers, progressSearch, tvNoResults)
                        true
                    } else {
                        Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show()
                        false
                    }
                } else {
                    false
                }
            }
            
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing search dialog", e)
            showError("Error showing search dialog: ${e.message}")
        }
    }
    
    // Helper to dismiss parent dialog
    private fun dismissParentDialog(view: View) {
        var parent: ViewParent? = view.parent
        while (parent != null) {
            if (parent is AlertDialog) {
                parent.dismiss()
                break
            }
            parent = parent.parent
        }
    }
    
    // Open chat activity
    private fun openChatActivity(conversation: Conversation) {
        if (!isAdded) return
        
        try {
            // Ensure we have a valid userId
            if (conversation.userId <= 0) {
                Log.e(TAG, "Invalid user ID in conversation: ${conversation.userId}")
                showError("Invalid user information in this conversation")
                return
            }
            
            // Create intent with all required extras
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("USER_ID", conversation.userId)
                putExtra("USER_NAME", conversation.userName)
                putExtra("USER_ROLE", conversation.userRole)
                putExtra("PROFILE_IMAGE", conversation.profileImage ?: "")
            }
            
            // Log the data being sent
            Log.d(TAG, "Opening chat with user: ID=${conversation.userId}, Name=${conversation.userName}")
            
            // Start the activity
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening chat activity", e)
            showError("Error opening chat: ${e.message}")
        }
    }
    
    // Show/hide loading indicator
    private fun showLoading(isLoading: Boolean) {
        if (!isAdded) return
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView?.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
    
    // Show/hide no chats message
    private fun showNoChats(isEmpty: Boolean) {
        if (!isAdded) return
        tvNoChats?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView?.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    // Show error message
    private fun showError(message: String) {
        if (!isAdded) return
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
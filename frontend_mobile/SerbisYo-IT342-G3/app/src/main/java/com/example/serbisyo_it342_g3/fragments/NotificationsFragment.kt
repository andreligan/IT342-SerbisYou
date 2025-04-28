package com.example.serbisyo_it342_g3.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.serbisyo_it342_g3.ChatActivity
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.adapters.NotificationAdapter
import com.example.serbisyo_it342_g3.api.MessageApiClient
import com.example.serbisyo_it342_g3.api.NotificationApiClient
import com.example.serbisyo_it342_g3.data.Notification
import com.google.android.material.snackbar.Snackbar

class NotificationsFragment : Fragment() {
    private val TAG = "NotificationsFragment"
    
    // UI Components
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoNotifications: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var btnMarkAllAsRead: Button
    
    // Data
    private lateinit var notificationApiClient: NotificationApiClient
    private lateinit var messageApiClient: MessageApiClient
    private var userId: Long = 0
    private var token: String = ""
    private val notifications = mutableListOf<Notification>()
    
    private lateinit var adapter: NotificationAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        
        // Initialize UI components
        recyclerView = view.findViewById(R.id.recyclerViewNotifications)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        btnMarkAllAsRead = view.findViewById(R.id.btnMarkAllAsRead)
        
        // Get user ID and token from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getLong("userId", 0)
        token = sharedPreferences.getString("token", "") ?: ""
        
        Log.d(TAG, "User ID: $userId, Token length: ${token.length}")
        
        notificationApiClient = NotificationApiClient(requireContext())
        messageApiClient = MessageApiClient(requireContext())
        
        // Set up RecyclerView
        setupRecyclerView()
        
        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            loadNotifications()
        }
        
        // Set up Mark All as Read button
        btnMarkAllAsRead.setOnClickListener {
            markAllNotificationsAsRead()
        }
        
        // Load notifications
        loadNotifications()
        
        return view
    }
    
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationAdapter(requireContext(), notifications) { notification ->
            handleNotificationClick(notification)
        }
        recyclerView.adapter = adapter
    }
    
    private fun loadNotifications() {
        Log.d(TAG, "loadNotifications called, loading for userId: $userId")
        progressBar.visibility = View.VISIBLE
        tvNoNotifications.visibility = View.GONE
        
        // Check for valid token and userId
        if (token.isBlank() || userId == 0L) {
            Log.e(TAG, "Invalid token or userId. Token length: ${token.length}, userId: $userId")
            progressBar.visibility = View.GONE
            tvNoNotifications.visibility = View.VISIBLE
            tvNoNotifications.text = "Please login to view notifications"
            return
        }
        
        notificationApiClient.getNotificationsByUserId(userId, token) { notificationsList, error ->
            if (activity == null) return@getNotificationsByUserId // Fragment not attached
            
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
                
                if (error != null) {
                    Log.e(TAG, "Error loading notifications", error)
                    tvNoNotifications.visibility = View.VISIBLE
                    tvNoNotifications.text = "Error loading notifications: ${error.message}"
                    recyclerView.visibility = View.GONE
                    btnMarkAllAsRead.visibility = View.GONE
                    return@runOnUiThread
                }
                
                val notifications = notificationsList ?: emptyList()
                
                // Detailed logging of notifications
                Log.d(TAG, "Received ${notifications.size} notifications")
                notifications.forEachIndexed { index, notification ->
                    Log.d(TAG, "Notification #$index: id=${notification.notificationId}, " +
                            "userId=${notification.userId}, " +
                            "type=${notification.type}, " +
                            "message=${notification.message}, " +
                            "user=${notification.user}")
                }
                
                if (notifications.isEmpty()) {
                    Log.d(TAG, "No notifications found for user $userId")
                    tvNoNotifications.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    btnMarkAllAsRead.visibility = View.GONE
                } else {
                    val unreadCount = notifications.count { !it.isRead && !it.read }
                    val unreadNotifications = unreadCount > 0
                    Log.d(TAG, "Found ${notifications.size} notifications, $unreadCount unread")
                    tvNoNotifications.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    btnMarkAllAsRead.visibility = if (unreadNotifications) View.VISIBLE else View.GONE
                    
                    // Update the member variable with the new notifications list
                    this@NotificationsFragment.notifications.clear()
                    this@NotificationsFragment.notifications.addAll(notifications)
                    adapter.updateNotifications(notifications)
                }
                
                // Notify activity to update the badge count
                updateNotificationBadge()
            }
        }
    }
    
    private fun markAllNotificationsAsRead() {
        progressBar.visibility = View.VISIBLE
        btnMarkAllAsRead.isEnabled = false
        
        notificationApiClient.markAllAsRead(userId, token) { success, error ->
            if (activity == null) return@markAllAsRead // Fragment not attached
            
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                btnMarkAllAsRead.isEnabled = true
                
                if (!success) {
                    Log.e(TAG, "Failed to mark all notifications as read", error)
                    Snackbar.make(
                        requireView(),
                        "Failed to mark all notifications as read",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@runOnUiThread
                }
                
                // Reload notifications to refresh the UI
                loadNotifications()
                
                // Show success message
                Snackbar.make(
                    requireView(),
                    "All notifications marked as read",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun handleNotificationClick(notification: Notification) {
        // Skip if notification is already read
        if (notification.isRead || notification.read) {
            // Proceed directly to handling the notification type
            handleNotificationByType(notification)
            return
        }
        
        // Mark notification as read
        notificationApiClient.markNotificationAsRead(notification.notificationId, token) { success, error ->
            if (error != null) {
                Log.e(TAG, "Failed to mark notification as read", error)
                // Still proceed to handle the notification
                requireActivity().runOnUiThread {
                    handleNotificationByType(notification)
                }
                return@markNotificationAsRead
            }
            
            if (!success) {
                Log.e(TAG, "Failed to mark notification as read (server returned false)")
                // Still proceed to handle the notification
                requireActivity().runOnUiThread {
                    handleNotificationByType(notification)
                }
                return@markNotificationAsRead
            }
            
            // Update local data
            val index = notifications.indexOfFirst { it.notificationId == notification.notificationId }
            if (index >= 0) {
                val updatedNotification = notification.copy(isRead = true, read = true)
                notifications[index] = updatedNotification
                requireActivity().runOnUiThread {
                    adapter.notifyItemChanged(index)
                    
                    // Update badge count
                    updateNotificationBadge()
                    
                    // Handle navigation based on notification type
                    handleNotificationByType(notification)
                }
            } else {
                // Notification not found in local list, still handle it
                requireActivity().runOnUiThread {
                    handleNotificationByType(notification)
                }
            }
        }
    }
    
    private fun handleNotificationByType(notification: Notification) {
        // Handle navigation based on notification type and referenceType
        val type = notification.type?.lowercase() ?: notification.referenceType.lowercase()
        
        when (type) {
            "message" -> handleMessageNotification(notification)
            "booking" -> handleBookingNotification(notification)
            "transaction" -> handleTransactionNotification(notification)
            "review" -> handleReviewNotification(notification)
            else -> {
                Log.d(TAG, "No specific handler for notification type: $type")
                // Show a toast message that this notification type is not supported yet
                Toast.makeText(requireContext(), 
                    "This notification type is not supported yet", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun handleMessageNotification(notification: Notification) {
        // For message notifications, we need to extract the senderId from the message content
        // or use the referenceId to get the message details
        getSenderFromMessageId(notification.referenceId) { senderId, senderName ->
            if (senderId > 0) {
                requireActivity().runOnUiThread {
                    openChatWithUser(senderId, senderName)
                }
            } else {
                // If we couldn't get sender info, show an error
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), 
                        "Could not find the message sender", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun handleBookingNotification(notification: Notification) {
        // Navigate to booking details
        val bookingId = notification.referenceId
        if (bookingId > 0) {
            Log.d(TAG, "Navigate to booking details for booking ID: $bookingId")
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), 
                    "Booking details navigation not implemented yet", 
                    Toast.LENGTH_SHORT).show()
            }
            // TODO: Implement navigation to booking details screen
            // For example:
            // val intent = Intent(requireContext(), BookingDetailsActivity::class.java).apply {
            //     putExtra("BOOKING_ID", bookingId)
            // }
            // startActivity(intent)
        }
    }
    
    private fun handleTransactionNotification(notification: Notification) {
        // Navigate to transaction details 
        val transactionId = notification.referenceId
        if (transactionId > 0) {
            Log.d(TAG, "Navigate to transaction details for transaction ID: $transactionId")
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), 
                    "Transaction details navigation not implemented yet", 
                    Toast.LENGTH_SHORT).show()
            }
            // TODO: Implement navigation to transaction details screen
            // For example:
            // val intent = Intent(requireContext(), TransactionDetailsActivity::class.java).apply {
            //     putExtra("TRANSACTION_ID", transactionId)
            // }
            // startActivity(intent)
        }
    }
    
    private fun handleReviewNotification(notification: Notification) {
        // Navigate to service details to show review
        val reviewId = notification.referenceId
        if (reviewId > 0) {
            Log.d(TAG, "Navigate to review details for review ID: $reviewId")
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), 
                    "Review details navigation not implemented yet", 
                    Toast.LENGTH_SHORT).show()
            }
            // TODO: Implement navigation to review details screen
            // For example:
            // val intent = Intent(requireContext(), ServiceDetailsActivity::class.java).apply {
            //     putExtra("REVIEW_ID", reviewId)
            // }
            // startActivity(intent)
        }
    }
    
    private fun getSenderFromMessageId(messageId: Long, callback: (Long, String) -> Unit) {
        // Get the message details to find the sender ID
        if (messageId <= 0) {
            Log.e(TAG, "Invalid message ID: $messageId")
            callback(0, "User")
            return
        }
        
        messageApiClient.getMessageById(messageId, token) { message, error ->
            if (error != null || message == null) {
                Log.e(TAG, "Failed to get message details: ${error?.message}")
                callback(0, "User")
                return@getMessageById
            }
            
            // Always use the original sender information, not the "other person" in the conversation
            val senderId = message.senderId
            val senderName = message.senderName ?: "User"
            
            Log.d(TAG, "Message sender: ID=$senderId, Name=$senderName")
            
            callback(senderId, senderName)
        }
    }
    
    private fun openChatWithUser(otherUserId: Long, otherUserName: String) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("USER_ID", otherUserId)
            putExtra("USER_NAME", otherUserName)
            putExtra("USER_ROLE", "")  // We don't have this info, so pass empty string
        }
        startActivity(intent)
    }
    
    private fun updateNotificationBadge() {
        val unreadCount = notifications.count { !it.isRead && !it.read }
        Log.d(TAG, "Unread notification count: $unreadCount")
        
        // Use an interface or shared ViewModel to communicate with activity
        (requireActivity() as? NotificationBadgeListener)?.updateNotificationBadge(unreadCount)
    }
    
    // Interface for communication with activity
    interface NotificationBadgeListener {
        fun updateNotificationBadge(count: Int)
    }
    
    override fun onResume() {
        super.onResume()
        loadNotifications() // Reload notifications when fragment becomes visible again
    }
    
    // Add this helper method
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
    
    companion object {
        @JvmStatic
        fun newInstance(): NotificationsFragment {
            return NotificationsFragment()
        }
    }
}
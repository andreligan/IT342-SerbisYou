package com.example.serbisyo_it342_g3.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.NotificationApiClient
import com.example.serbisyo_it342_g3.data.Notification
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment : Fragment() {
    private val TAG = "NotificationsFragment"
    
    private lateinit var rvNotifications: RecyclerView
    private lateinit var tvNoNotifications: TextView
    private lateinit var progressBar: ProgressBar
    
    private lateinit var notificationApiClient: NotificationApiClient
    private var notifications = mutableListOf<Notification>()
    private lateinit var notificationAdapter: NotificationAdapter
    private var token: String = ""
    private var userId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        
        // Get shared preferences
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        userId = try {
            // Try to get as Long first (new format)
            sharedPref.getLong("userId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            val userIdStr = sharedPref.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        token = sharedPref.getString("token", "") ?: ""
        
        // Initialize the API client
        notificationApiClient = NotificationApiClient(requireContext())
        
        // Initialize views
        rvNotifications = view.findViewById(R.id.rvNotifications)
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications)
        progressBar = view.findViewById(R.id.progressBar)
        
        // Set up RecyclerView
        rvNotifications.layoutManager = LinearLayoutManager(context)
        notificationAdapter = NotificationAdapter(notifications) { notification ->
            markNotificationAsRead(notification)
        }
        rvNotifications.adapter = notificationAdapter
        
        // Load notifications
        loadNotifications()
        
        return view
    }
    
    private fun loadNotifications() {
        progressBar.visibility = View.VISIBLE
        rvNotifications.visibility = View.GONE
        tvNoNotifications.visibility = View.GONE
        
        notificationApiClient.getNotificationsByUserId(userId, token) { notificationList, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading notifications", error)
                    Toast.makeText(context, "Error loading notifications: ${error.message}", Toast.LENGTH_SHORT).show()
                    tvNoNotifications.text = "Failed to load notifications"
                    tvNoNotifications.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                
                notifications.clear()
                if (notificationList != null && notificationList.isNotEmpty()) {
                    // Sort notifications by date (newest first)
                    val sortedNotifications = notificationList.sortedByDescending { it.createdAt }
                    notifications.addAll(sortedNotifications)
                    rvNotifications.visibility = View.VISIBLE
                    tvNoNotifications.visibility = View.GONE
                } else {
                    tvNoNotifications.visibility = View.VISIBLE
                    rvNotifications.visibility = View.GONE
                }
                
                notificationAdapter.notifyDataSetChanged()
            }
        }
    }
    
    private fun markNotificationAsRead(notification: Notification) {
        if (!notification.isRead) {
            notificationApiClient.markNotificationAsRead(notification.notificationId, token) { success, error ->
                if (success) {
                    // Update the notification in the list
                    val index = notifications.indexOfFirst { it.notificationId == notification.notificationId }
                    if (index != -1) {
                        // We can't directly modify the Notification object since data classes are immutable
                        // Create a new copy with isRead = true
                        val updatedNotification = notification.copy(isRead = true)
                        notifications[index] = updatedNotification
                        requireActivity().runOnUiThread {
                            notificationAdapter.notifyItemChanged(index)
                        }
                    }
                } else {
                    Log.e(TAG, "Error marking notification as read", error)
                }
            }
        }
    }
    
    // Adapter for the RecyclerView
    inner class NotificationAdapter(
        private val notifications: List<Notification>,
        private val onNotificationClick: (Notification) -> Unit
    ) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    
        inner class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivNotificationType: ImageView = view.findViewById(R.id.ivNotificationType)
            val tvNotificationMessage: TextView = view.findViewById(R.id.tvNotificationMessage)
            val tvNotificationTime: TextView = view.findViewById(R.id.tvNotificationTime)
            val unreadIndicator: View = view.findViewById(R.id.unreadIndicator)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            return NotificationViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
            val notification = notifications[position]
            
            holder.tvNotificationMessage.text = notification.message
            
            // Set icon based on type
            when (notification.type?.lowercase()) {
                "booking" -> holder.ivNotificationType.setImageResource(R.drawable.ic_notifications)
                "message" -> holder.ivNotificationType.setImageResource(R.drawable.ic_chat)
                else -> holder.ivNotificationType.setImageResource(R.drawable.ic_notifications)
            }
            
            // Format the time
            holder.tvNotificationTime.text = formatDateTime(notification.createdAt)
            
            // Set unread indicator
            holder.unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE
            
            // Set background color based on read status
            holder.itemView.setBackgroundColor(
                if (notification.isRead) 
                    android.graphics.Color.WHITE 
                else 
                    android.graphics.Color.parseColor("#F5F5F5")
            )
            
            // Set click listener
            holder.itemView.setOnClickListener {
                onNotificationClick(notification)
            }
        }
        
        override fun getItemCount() = notifications.size
        
        private fun formatDateTime(dateTimeStr: String?): String {
            if (dateTimeStr == null) return "Unknown time"
            
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val date = inputFormat.parse(dateTimeStr) ?: return "Unknown time"
                
                val now = Calendar.getInstance()
                val notificationTime = Calendar.getInstance()
                notificationTime.time = date
                
                return when {
                    // Today
                    now.get(Calendar.YEAR) == notificationTime.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == notificationTime.get(Calendar.DAY_OF_YEAR) -> {
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        "Today at " + timeFormat.format(date)
                    }
                    // Yesterday
                    now.get(Calendar.YEAR) == notificationTime.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) - notificationTime.get(Calendar.DAY_OF_YEAR) == 1 -> {
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        "Yesterday at " + timeFormat.format(date)
                    }
                    // Within this week
                    now.get(Calendar.YEAR) == notificationTime.get(Calendar.YEAR) &&
                    now.get(Calendar.WEEK_OF_YEAR) == notificationTime.get(Calendar.WEEK_OF_YEAR) -> {
                        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        dayFormat.format(date) + " at " + timeFormat.format(date)
                    }
                    // This year
                    now.get(Calendar.YEAR) == notificationTime.get(Calendar.YEAR) -> {
                        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                        dateFormat.format(date)
                    }
                    // Older
                    else -> {
                        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        dateFormat.format(date)
                    }
                }
            } catch (e: ParseException) {
                Log.e(TAG, "Error parsing date", e)
                return "Unknown time"
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadNotifications()
    }
    
    companion object {
        fun newInstance() = NotificationsFragment()
    }
} 
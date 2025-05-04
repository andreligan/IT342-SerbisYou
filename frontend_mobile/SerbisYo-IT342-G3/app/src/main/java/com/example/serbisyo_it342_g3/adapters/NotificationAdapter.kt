package com.example.serbisyo_it342_g3.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.MessageApiClient
import com.example.serbisyo_it342_g3.data.Message
import com.example.serbisyo_it342_g3.data.Notification
import com.example.serbisyo_it342_g3.data.UserData
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.Calendar

class NotificationAdapter(
    private val context: Context,
    private var notifications: List<Notification>,
    private val onNotificationClicked: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val TAG = "NotificationAdapter"
    
    // Get current user ID from SharedPreferences
    private val currentUserId = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        .getLong("userId", 0)
        
    // Get token from SharedPreferences
    private val token = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        .getString("token", "") ?: ""
        
    // Create MessageApiClient instance to fetch message details
    private val messageApiClient = MessageApiClient(context)
    
    // Cache for message sender info to avoid redundant API calls
    private val messageSenderCache = mutableMapOf<Long, Pair<Long, String>>() // messageId -> (senderId, senderName)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size
    
    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfileImage: CircleImageView = itemView.findViewById(R.id.ivProfileImage)
        private val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        
        // Date format for parsing createdAt timestamps with milliseconds component
        private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        // Fallback date format without milliseconds
        private val fallbackIsoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        // Simple format for dates from API
        private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        // Display format for older dates
        private val displayDateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
        // Time-only format for same-day dates
        private val timeOnlyFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        // Date-only format for different years
        private val dateOnlyFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        fun bind(notification: Notification) {
            // Log timestamp for debugging
            Log.d(TAG, "Notification ${notification.notificationId} createdAt: ${notification.createdAt}")
            
            // Set appropriate icon and title based on notification type
            when (notification.type?.lowercase() ?: notification.referenceType.lowercase()) {
                "message" -> setupMessageNotification(notification)
                "booking" -> setupBookingNotification(notification)
                "transaction" -> setupTransactionNotification(notification)
                "review" -> setupReviewNotification(notification)
                else -> setupGenericNotification(notification)
            }
            
            // Format time with more detailed logging
            val timeAgo = getTimeAgo(notification.createdAt)
            tvTime.text = timeAgo
            Log.d(TAG, "Notification ${notification.notificationId} formatted time: $timeAgo")
            
            // Show/hide unread indicator based on isRead or read property
            val isRead = notification.isRead || notification.read
            unreadIndicator.visibility = if (isRead) View.GONE else View.VISIBLE
            
            // Set click listener
            itemView.setOnClickListener {
                onNotificationClicked(notification)
            }
        }

        private fun setupMessageNotification(notification: Notification) {
            // Log initial notification data for debugging
            Log.d(TAG, "Processing message notification: id=${notification.notificationId}, " +
                    "referenceId=${notification.referenceId}, " + 
                    "message=${notification.message}")
            
            // First set default values that will be shown while we fetch the real sender info
            tvSenderName.text = "Message"
            tvMessage.text = notification.message ?: "New message"
            ivProfileImage.setImageResource(R.drawable.ic_message_notification)
            
            // If we have the messageId (referenceId), use it to get the real sender info
            if (notification.referenceId > 0) {
                fetchAndSetMessageSenderInfo(notification)
            } else {
                // Try to extract sender info from the notification message
                extractSenderFromMessage(notification.message ?: "")?.let { senderName ->
                    tvSenderName.text = senderName
                    tvMessage.text = "sent you a message"
                }
            }
        }
        
        private fun fetchAndSetMessageSenderInfo(notification: Notification) {
            val messageId = notification.referenceId
            
            // Check if we already have this message's sender info in cache
            if (messageSenderCache.containsKey(messageId)) {
                val (senderId, senderName) = messageSenderCache[messageId]!!
                displayMessageSender(senderName, notification.message)
                return
            }
            
            // Fetch message details to get the sender
            messageApiClient.getMessageById(messageId, token) { message, error ->
                if (error != null || message == null) {
                    Log.e(TAG, "Failed to get message details: ${error?.message}")
                    return@getMessageById
                }
                
                // Get the sender name - this is the real name of the person who sent the message
                val senderId = message.senderId
                val senderName = message.senderName ?: "User $senderId"
                
                // Cache this information for future use
                messageSenderCache[messageId] = Pair(senderId, senderName)
                
                // Update UI on main thread
                itemView.post {
                    displayMessageSender(senderName, notification.message)
                }
            }
        }
        
        private fun displayMessageSender(senderName: String, messageText: String?) {
            // Set the UI elements with the real sender name
            tvSenderName.text = senderName
            
            // Extract just the message content without the sender prefix if possible
            val message = extractMessageContent(messageText, senderName)
            tvMessage.text = message
            
            // Log the final display values
            Log.d(TAG, "Final display values - senderName: $senderName, message: $message")
        }
        
        private fun extractMessageContent(messageText: String?, senderName: String): String {
            if (messageText.isNullOrEmpty()) return "sent you a message"
            
            // Try to extract just the message content if it follows pattern "X sent you a message: [content]"
            val regex = ".*sent you a message: (.+)".toRegex()
            val match = regex.find(messageText)
            
            return if (match != null && match.groupValues.size > 1) {
                // Return just the content part
                "sent you: ${match.groupValues[1]}"
            } else if (messageText.contains("sent you a message")) {
                // If it's a generic "sent you a message" without content
                "sent you a message"
            } else {
                // Otherwise return the whole message
                messageText
            }
        }
        
        private fun extractSenderFromMessage(messageText: String): String? {
            // Try to match pattern "[SenderName] sent you a message"
            val regex = "(.+?) sent you a message".toRegex()
            val match = regex.find(messageText)
            
            return if (match != null && match.groupValues.size > 1) {
                val extractedName = match.groupValues[1]
                if (extractedName != "Someone") extractedName else null
            } else {
                null
            }
        }
        
        private fun setupBookingNotification(notification: Notification) {
            tvSenderName.text = "Booking Update"
            tvMessage.text = notification.message ?: "You have a new booking request"
            
            // Set booking icon
            ivProfileImage.setImageResource(R.drawable.ic_booking_notification)
        }
        
        private fun setupTransactionNotification(notification: Notification) {
            tvSenderName.text = "Payment Update"
            tvMessage.text = notification.message ?: "You have a new payment transaction"
            
            // Set transaction icon
            ivProfileImage.setImageResource(R.drawable.ic_payment_notification)
        }
        
        private fun setupReviewNotification(notification: Notification) {
            tvSenderName.text = "New Review"
            tvMessage.text = notification.message ?: "You have received a new review"
            
            // Set review icon
            ivProfileImage.setImageResource(R.drawable.ic_review_notification)
        }
        
        private fun setupGenericNotification(notification: Notification) {
            // Try to determine type from referenceType if available
            val type = when (notification.referenceType.lowercase()) {
                "message" -> "Message"
                "booking" -> "Booking"
                "transaction" -> "Payment"
                "review" -> "Review"
                else -> notification.type?.capitalize() ?: "Notification"
            }
            
            tvSenderName.text = type
            tvMessage.text = notification.message ?: "You have a new notification"
            
            // Choose icon based on reference type
            val iconResId = when (notification.referenceType.lowercase()) {
                "message" -> R.drawable.ic_message_notification
                "booking" -> R.drawable.ic_booking_notification
                "transaction" -> R.drawable.ic_payment_notification
                "review" -> R.drawable.ic_review_notification
                else -> R.drawable.default_profile
            }
            
            ivProfileImage.setImageResource(iconResId)
        }
        
        private fun String.capitalize(): String {
            return this.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
        }
        
        private fun getTimeAgo(createdAtStr: String): String {
            Log.d(TAG, "Processing timestamp: $createdAtStr")
            
            if (createdAtStr.isEmpty()) {
                Log.d(TAG, "Empty timestamp, returning 'Recently'")
                return "Recently"
            }

            try {
                // Parse the date string into a Date object
                val createdAtDate = parseDate(createdAtStr)
                if (createdAtDate == null) {
                    Log.e(TAG, "Could not parse date: $createdAtStr")
                    return "Recently"
                }
                
                // Get current date for comparison
                val now = Date()
                
                // Calculate time difference in milliseconds
                val diffMillis = now.time - createdAtDate.time
                
                // Convert to various time units
                val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
                val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
                
                // Formatting logic with more detailed output for debugging
                val result = when {
                    diffMinutes < 1 -> "Just now"
                    diffMinutes < 60 -> "$diffMinutes min ago"
                    diffHours < 24 -> "$diffHours hour${if (diffHours > 1) "s" else ""} ago"
                    diffDays < 7 -> "$diffDays day${if (diffDays > 1) "s" else ""} ago"
                    else -> {
                        // For older messages, show actual date
                        val nowCalendar = Calendar.getInstance()
                        val createdCalendar = Calendar.getInstance().apply { time = createdAtDate }
                        
                        if (nowCalendar.get(Calendar.YEAR) == createdCalendar.get(Calendar.YEAR)) {
                            // Same year, show month and day
                            dateOnlyFormat.format(createdAtDate)
                        } else {
                            // Different year, include year
                            dateOnlyFormat.format(createdAtDate)
                        }
                    }
                }
                
                Log.d(TAG, "Timestamp $createdAtStr formatted as: $result")
                return result
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating time ago for $createdAtStr", e)
                return "Recently"
            }
        }
        
        private fun parseDate(dateStr: String): Date? {
            // First try standard formats
            try {
                // Try the ISO format with milliseconds
                return isoDateFormat.parse(dateStr)
            } catch (e: ParseException) {
                try {
                    // Try the ISO format without milliseconds
                    return fallbackIsoDateFormat.parse(dateStr)
                } catch (e2: ParseException) {
                    try {
                        // Try simple date format (from our deserializer)
                        return simpleDateFormat.parse(dateStr)
                    } catch (e3: ParseException) {
                        // None of our formats worked, log the error
                        Log.e(TAG, "Failed to parse date with standard formats: $dateStr")
                    }
                }
            }
            
            // If we get here, try to parse array format directly
            return try {
                // Check if the string looks like an array representation: [2025,4,30,9,44,22]
                if (dateStr.startsWith("[") && dateStr.endsWith("]")) {
                    val parts = dateStr.substring(1, dateStr.length - 1).split(",")
                    if (parts.size >= 3) {
                        // Extract date components
                        val year = parts[0].trim().toInt()
                        val month = parts[1].trim().toInt() - 1 // Month is 0-based in Calendar
                        val day = parts[2].trim().toInt()
                        
                        // Initialize calendar with date
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, day)
                        
                        // Add time if available
                        if (parts.size >= 6) {
                            val hour = parts[3].trim().toInt()
                            val minute = parts[4].trim().toInt()
                            val second = parts[5].trim().toInt()
                            
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            calendar.set(Calendar.SECOND, second)
                        }
                        
                        calendar.time
                    } else {
                        Log.e(TAG, "Invalid array date format: $dateStr")
                        null
                    }
                } else {
                    Log.e(TAG, "Unknown date format: $dateStr")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing date array: $dateStr", e)
                null
            }
        }
    }
}
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
import com.example.serbisyo_it342_g3.data.Notification
import com.example.serbisyo_it342_g3.data.UserData
import com.example.serbisyo_it342_g3.utils.Constants
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationAdapter(
    private val context: Context,
    private var notifications: List<Notification>,
    private val onNotificationClicked: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val TAG = "NotificationAdapter"
    
    // Get current user ID from SharedPreferences
    private val currentUserId = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        .getLong("userId", 0)

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
        private val displayDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        fun bind(notification: Notification) {
            // Set appropriate icon and title based on notification type
            when (notification.type?.lowercase() ?: notification.referenceType.lowercase()) {
                "message" -> setupMessageNotification(notification)
                "booking" -> setupBookingNotification(notification)
                "transaction" -> setupTransactionNotification(notification)
                "review" -> setupReviewNotification(notification)
                else -> setupGenericNotification(notification)
            }
            
            // Format time
            val timeAgo = getTimeAgo(notification.createdAt)
            tvTime.text = timeAgo
            
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
                    "senderName=${notification.senderName}, message=${notification.message}, " +
                    "user=${notification.user}")
            
            // Try to get sender name from multiple sources in priority order
            var senderName = when {
                // 1. Use senderName if it's already populated and not empty
                !notification.senderName.isNullOrEmpty() -> {
                    Log.d(TAG, "Using provided senderName: ${notification.senderName}")
                    notification.senderName
                }
                
                // 2. Extract from user object if it's a Map (from JSON)
                notification.user is Map<*, *> -> {
                    val userName = (notification.user as Map<*, *>)["userName"] as? String
                        ?: (notification.user as Map<*, *>)["name"] as? String
                    Log.d(TAG, "Extracted userName from Map: $userName")
                    userName ?: "User"
                }
                
                // 3. Try to convert user object to UserData class if possible
                notification.user != null -> {
                    try {
                        val gson = Gson()
                        val userJson = gson.toJson(notification.user)
                        val userData = gson.fromJson(userJson, UserData::class.java)
                        Log.d(TAG, "Converted user object to UserData: ${userData.userName}")
                        userData.userName.ifEmpty { "User" }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to convert user object to UserData", e)
                        "User"
                    }
                }
                
                // 4. Extract from message content if it follows pattern "X sent you a message"
                notification.message?.contains(" sent you a message") == true -> {
                    val extractedName = notification.message.substringBefore(" sent you a message")
                    Log.d(TAG, "Extracted name from message: $extractedName")
                    extractedName
                }
                
                // 5. Fallback
                else -> {
                    Log.d(TAG, "Using default sender name")
                    "User"
                }
            }
            
            // Format the message text
            val messageText = when {
                // Use provided message if not empty
                !notification.message.isNullOrEmpty() -> {
                    // If message contains "sent you a message", clean it up to avoid repetition
                    if (notification.message.contains("sent you a message")) {
                        "sent you a message"
                    } else {
                        notification.message
                    }
                }
                // Default fallback message
                else -> "sent you a message"
            }
            
            // Set the UI elements
            tvSenderName.text = senderName
            tvMessage.text = messageText
            
            // Set the profile image
            if (!notification.senderProfileImage.isNullOrEmpty()) {
                Log.d(TAG, "Loading profile image: ${notification.senderProfileImage}")
                Glide.with(context)
                    .load(notification.senderProfileImage)
                    .placeholder(R.drawable.ic_message_notification)
                    .error(R.drawable.ic_message_notification)
                    .into(ivProfileImage)
            } else {
                ivProfileImage.setImageResource(R.drawable.ic_message_notification)
            }
            
            // Log the final display values
            Log.d(TAG, "Final display values - senderName: $senderName, message: $messageText")
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
            return try {
                // Parse the date string into a Date object, trying both date formats
                val createdAtDate = try {
                    isoDateFormat.parse(createdAtStr)
                } catch (e: ParseException) {
                    try {
                        fallbackIsoDateFormat.parse(createdAtStr)
                    } catch (e2: ParseException) {
                        Log.e(TAG, "Failed to parse date: $createdAtStr", e2)
                        return "Recently" // Return a default value if parsing fails
                    }
                } ?: Date()
                
                val now = Date()
                
                // Calculate time difference in milliseconds
                val diffMillis = now.time - createdAtDate.time
                
                // Convert to minutes, hours and days
                val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
                val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
                
                when {
                    diffMinutes < 1 -> "Just now"
                    diffMinutes < 60 -> "$diffMinutes min ago"
                    diffHours < 24 -> "$diffHours hour${if (diffHours > 1) "s" else ""} ago"
                    diffDays < 7 -> "$diffDays day${if (diffDays > 1) "s" else ""} ago"
                    else -> {
                        displayDateFormat.format(createdAtDate)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating time ago", e)
                "Recently"
            }
        }
    }
}
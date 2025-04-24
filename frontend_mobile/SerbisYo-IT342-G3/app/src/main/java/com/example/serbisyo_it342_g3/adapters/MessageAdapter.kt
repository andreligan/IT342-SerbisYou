package com.example.serbisyo_it342_g3.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Message
import com.example.serbisyo_it342_g3.data.MessageStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val context: Context,
    private val messages: List<Message>,
    private val currentUserId: Long,
    private val onRetryClick: ((Message) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val statusIcon: ImageView = itemView.findViewById(R.id.ivMessageStatus)
        val retryButton: ImageButton = itemView.findViewById(R.id.btnRetry)
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val senderName: TextView? = itemView.findViewById(R.id.tvSenderName)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        
        if (holder.itemViewType == VIEW_TYPE_SENT) {
            val viewHolder = holder as SentMessageViewHolder
            viewHolder.messageContent.text = message.content
            viewHolder.timestamp.text = formatTime(message.timestamp)
            
            // Set message status indicator
            setMessageStatus(viewHolder, message)
            
            // Set up retry button for failed messages
            if (message.status == MessageStatus.ERROR) {
                viewHolder.retryButton.visibility = View.VISIBLE
                viewHolder.retryButton.setOnClickListener {
                    onRetryClick?.invoke(message)
                }
            } else {
                viewHolder.retryButton.visibility = View.GONE
            }
        } else {
            val viewHolder = holder as ReceivedMessageViewHolder
            viewHolder.messageContent.text = message.content
            viewHolder.timestamp.text = formatTime(message.timestamp)
            
            // Show sender name if available (optional, depends on your UI)
            if (message.senderName != null && viewHolder.senderName != null) {
                viewHolder.senderName.text = message.senderName
                viewHolder.senderName.visibility = View.VISIBLE
            } else if (viewHolder.senderName != null) {
                viewHolder.senderName.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    /**
     * Format timestamp to match web version
     * - If today: show time only (e.g. "15:30")
     * - If yesterday: show "Yesterday"
     * - If this week: show day name (e.g. "Monday")
     * - Otherwise: show date (e.g. "Apr 22")
     */
    private fun formatTime(date: Date): String {
        val now = System.currentTimeMillis()
        val time = date.time
        
        return when {
            // Today
            DateUtils.isToday(time) -> {
                timeFormat.format(date)
            }
            // Yesterday
            DateUtils.isToday(time + DateUtils.DAY_IN_MILLIS) -> {
                "Yesterday"
            }
            // Within last week
            now - time < 7 * DateUtils.DAY_IN_MILLIS -> {
                DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_WEEKDAY)
            }
            // Older messages
            else -> {
                dateFormat.format(date)
            }
        }
    }
    
    /**
     * Set the appropriate status icon based on message status
     */
    private fun setMessageStatus(holder: SentMessageViewHolder, message: Message) {
        val statusIcon = holder.statusIcon
        
        when (message.status) {
            MessageStatus.SENDING -> {
                // Use system progress spinner icon
                statusIcon.setImageResource(android.R.drawable.ic_popup_sync)
                statusIcon.visibility = View.VISIBLE
            }
            MessageStatus.SENT -> {
                // Use a single checkmark for sent 
                statusIcon.setImageResource(android.R.drawable.ic_menu_send)
                statusIcon.visibility = View.VISIBLE
            }
            MessageStatus.DELIVERED -> {
                // Use system "done" icon
                statusIcon.setImageResource(android.R.drawable.ic_menu_send)
                statusIcon.visibility = View.VISIBLE
            }
            MessageStatus.READ -> {
                // Use system "done all" icon
                statusIcon.setImageResource(android.R.drawable.checkbox_on_background)
                statusIcon.visibility = View.VISIBLE
            }
            MessageStatus.ERROR -> {
                // Use system error icon
                statusIcon.setImageResource(android.R.drawable.ic_dialog_alert)
                statusIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                statusIcon.visibility = View.VISIBLE
            }
        }
    }
}
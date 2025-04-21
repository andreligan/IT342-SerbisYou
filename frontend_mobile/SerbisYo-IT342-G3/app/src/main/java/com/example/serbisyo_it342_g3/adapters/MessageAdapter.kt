package com.example.serbisyo_it342_g3.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Message
import java.util.Date

class MessageAdapter(
    private val context: Context,
    private val messages: List<Message>,
    private val currentUserId: Long
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
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
            viewHolder.timestamp.text = getRelativeTimeSpan(message.timestamp)
        } else {
            val viewHolder = holder as ReceivedMessageViewHolder
            viewHolder.messageContent.text = message.content
            viewHolder.timestamp.text = getRelativeTimeSpan(message.timestamp)
            
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

    private fun getRelativeTimeSpan(date: Date): String {
        val now = System.currentTimeMillis()
        val time = date.time
        
        // Use a shorter format for messages
        // If within 24 hours, show time only (e.g. "10:30 AM")
        // If older, show relative ("Yesterday", "Monday", etc.)
        return if (now - time < DateUtils.DAY_IN_MILLIS) {
            android.text.format.DateFormat.getTimeFormat(context).format(date)
        } else {
            DateUtils.getRelativeTimeSpanString(
                time, now, DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
        }
    }
} 
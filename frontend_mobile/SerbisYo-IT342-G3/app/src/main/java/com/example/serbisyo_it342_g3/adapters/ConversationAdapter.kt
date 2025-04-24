package com.example.serbisyo_it342_g3.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Conversation
import java.util.Date
import android.net.Uri
import android.util.Log

class ConversationAdapter(
    private val context: Context,
    private val conversations: List<Conversation>,
    private val listener: OnConversationClickListener
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    interface OnConversationClickListener {
        fun onConversationClick(conversation: Conversation)
    }

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.ivProfilePic)
        val userName: TextView = itemView.findViewById(R.id.tvUserName)
        val lastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val lastMessageTime: TextView = itemView.findViewById(R.id.tvTime)
        val unreadCount: TextView = itemView.findViewById(R.id.tvUnreadCount)
        val userRole: TextView = itemView.findViewById(R.id.tvUserRole)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onConversationClick(conversations[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]

        // Load profile image
        if (conversation.profileImage != null) {
            try {
                // Set default profile image
                holder.profileImage.setImageResource(R.drawable.ic_profile)
                
                // Try to load from URI if possible
                try {
                    val uri = Uri.parse(conversation.profileImage)
                    holder.profileImage.setImageURI(uri)
                } catch (e: Exception) {
                    Log.e("ConversationAdapter", "Error loading profile image URI", e)
                }
            } catch (e: Exception) {
                Log.e("ConversationAdapter", "Error loading profile image", e)
                holder.profileImage.setImageResource(R.drawable.ic_profile)
            }
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile)
        }

        // Set user name and role
        holder.userName.text = conversation.userName
        holder.userRole.text = conversation.userRole

        // Set last message
        holder.lastMessage.text = conversation.lastMessage

        // Format and set time
        holder.lastMessageTime.text = getRelativeTimeSpan(conversation.lastMessageTime)

        // Handle unread count
        if (conversation.unreadCount > 0) {
            holder.unreadCount.visibility = View.VISIBLE
            holder.unreadCount.text = conversation.unreadCount.toString()
            // Make last message text bold if there are unread messages
            holder.lastMessage.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            holder.unreadCount.visibility = View.GONE
            holder.lastMessage.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    override fun getItemCount(): Int = conversations.size

    private fun getRelativeTimeSpan(date: Date): String {
        val now = System.currentTimeMillis()
        val time = date.time
        return DateUtils.getRelativeTimeSpanString(
            time, now, DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
} 
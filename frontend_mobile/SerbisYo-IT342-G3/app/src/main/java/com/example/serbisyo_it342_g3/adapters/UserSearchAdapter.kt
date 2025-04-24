package com.example.serbisyo_it342_g3.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.UserSearchModel

/**
 * Adapter for displaying user search results in a RecyclerView
 */
class UserSearchAdapter(
    private var users: List<UserSearchModel> = emptyList(),
    private val onUserClickListener: OnUserClickListener
) : RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>() {

    interface OnUserClickListener {
        fun onUserClick(user: UserSearchModel)
    }

    private val TAG = "UserSearchAdapter"

    fun updateUsers(newUsers: List<UserSearchModel>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_search, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val userInfo: TextView = itemView.findViewById(R.id.userInfo)
        private val roleChip: TextView = itemView.findViewById(R.id.roleChip)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    Log.d(TAG, "User clicked at position $position: ${users[position].userName}")
                    onUserClickListener.onUserClick(users[position])
                }
            }
        }

        fun bind(user: UserSearchModel) {
            try {
                // Set display name - prioritize business name, then full name, then username
                val displayName = when {
                    !user.businessName.isNullOrEmpty() -> user.businessName
                    (!user.firstName.isNullOrEmpty() && !user.lastName.isNullOrEmpty()) -> "${user.firstName} ${user.lastName}"
                    else -> user.userName
                }
                userName.text = displayName

                // Set secondary info - use username or email
                val secondaryInfo = when {
                    !user.userName.isNullOrEmpty() && user.userName != displayName -> "@${user.userName}"
                    !user.email.isNullOrEmpty() -> user.email
                    else -> ""
                }
                userInfo.text = secondaryInfo

                // Set role chip
                roleChip.text = user.role
                
                // Set different background colors based on role
                val roleColor = when (user.role) {
                    "Customer" -> android.graphics.Color.parseColor("#4CAF50") // Green
                    "Service Provider" -> android.graphics.Color.parseColor("#2196F3") // Blue
                    else -> android.graphics.Color.parseColor("#9E9E9E") // Grey
                }
                roleChip.setBackgroundColor(roleColor)

                // Set default profile image - not using Glide
                profileImage.setImageResource(R.drawable.default_profile)
                
                // Debug logging
                Log.d(TAG, "Bound user: $displayName (${user.role})")
            } catch (e: Exception) {
                Log.e(TAG, "Error binding user view", e)
            }
        }
    }
}
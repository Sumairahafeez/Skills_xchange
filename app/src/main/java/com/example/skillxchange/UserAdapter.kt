package com.example.skillxchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skillxchange.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

data class ConnectionInfo(val status: String, val senderId: String)

class UserAdapter(
    private var userList: List<User>,
    private var connectionInfo: Map<String, ConnectionInfo>,
    private val currentUserId: String,
    private val onConnectClick: (User) -> Unit,
    private val onAcceptClick: (User) -> Unit,
    private val onChatClick: (User) -> Unit,
    private val onProfileClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.ivUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserSkill: TextView = itemView.findViewById(R.id.tvUserSkill)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val btnConnect: MaterialButton = itemView.findViewById(R.id.btnConnect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvUserName.text = user.name.ifEmpty { "SkillXchange Member" }
        
        holder.tvUserSkill.text = when {
            user.skills.isNotEmpty() -> user.skills.take(2).joinToString(" · ")
            user.teachSkills.isNotEmpty() -> user.teachSkills.take(2).joinToString(" · ")
            else -> user.tagline.ifEmpty { "Explorer" }
        }

        Glide.with(holder.itemView.context)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_user_placeholder)
            .into(holder.ivAvatar)

        val info = connectionInfo[user.uid]
        
        // Default Connect state
        holder.btnConnect.isEnabled = true
        holder.btnConnect.alpha = 1.0f
        holder.btnConnect.text = "Connect"
        holder.btnConnect.setOnClickListener { onConnectClick(user) }

        if (info != null) {
            when (info.status) {
                "accepted" -> {
                    holder.btnConnect.text = "Message"
                    holder.btnConnect.setOnClickListener { onChatClick(user) }
                }
                "pending" -> {
                    if (info.senderId == currentUserId) {
                        holder.btnConnect.text = "Pending"
                        holder.btnConnect.isEnabled = false
                        holder.btnConnect.alpha = 0.6f
                    } else {
                        holder.btnConnect.text = "Accept"
                        holder.btnConnect.setOnClickListener { onAcceptClick(user) }
                    }
                }
            }
        }

        // Entire card is clickable to view profile
        holder.itemView.setOnClickListener { onProfileClick(user) }
    }

    override fun getItemCount(): Int = userList.size

    fun updateData(newList: List<User>, newInfo: Map<String, ConnectionInfo>) {
        userList = newList
        connectionInfo = newInfo
        notifyDataSetChanged()
    }
}

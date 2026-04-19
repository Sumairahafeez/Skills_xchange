package com.example.skillxchange

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatListAdapter(
    private var users: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatRowVH>() {

    inner class ChatRowVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvChatUserName)
        val tvTagline: TextView = view.findViewById(R.id.tvChatUserTagline)
        val tvLastMsg: TextView = view.findViewById(R.id.tvLastMessage)
        val dot: View = view.findViewById(R.id.viewNotificationDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRowVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_row, parent, false)
        return ChatRowVH(view)
    }

    override fun onBindViewHolder(holder: ChatRowVH, position: Int) {
        val user = users[position]
        holder.tvName.text = user.name
        holder.tvTagline.text = user.tagline

        val context = holder.itemView.context
        val prefs = context.getSharedPreferences("skillsxchange_prefs", Context.MODE_PRIVATE)
        val currentUserId = prefs.getString("user_id", "") ?: ""

        val messages = ChatCache.load(context, currentUserId, user.id)
        val lastMsg = messages.lastOrNull()
        
        holder.tvLastMsg.text = if (lastMsg != null)
            lastMsg.text.take(40) + if (lastMsg.text.length > 40) "..." else ""
        else ""

        val isViewed = ChatCache.isViewed(context, currentUserId, user.id)
        
        if (!isViewed && lastMsg != null && lastMsg.senderId != currentUserId) {
            holder.tvName.setTypeface(null, Typeface.BOLD)
            holder.tvLastMsg.setTypeface(null, Typeface.BOLD)
            holder.tvLastMsg.setTextColor(context.getColor(R.color.color_text_primary))
            holder.dot.visibility = View.VISIBLE
        } else {
            holder.tvName.setTypeface(null, Typeface.NORMAL)
            holder.tvLastMsg.setTypeface(null, Typeface.NORMAL)
            holder.tvLastMsg.setTextColor(context.getColor(R.color.color_text_secondary))
            holder.dot.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { 
            ChatCache.markViewed(context, currentUserId, user.id, true)
            onClick(user) 
        }
    }

    override fun getItemCount() = users.size

    fun updateList(newList: List<User>) {
        users = newList
        notifyDataSetChanged()
    }
}

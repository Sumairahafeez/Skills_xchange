package com.example.skillxchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.Chat
import java.text.SimpleDateFormat
import java.util.Locale

class ChatListAdapter(
    private var chats: List<Chat>,
    private val currentUserId: String,
    private val onClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatRowVH>() {

    inner class ChatRowVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvChatUserName)
        val tvLastMsg: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvChatUserTagline) // Reusing tagline for time or tagline
        val dot: View = view.findViewById(R.id.viewNotificationDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRowVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_row, parent, false)
        return ChatRowVH(view)
    }

    override fun onBindViewHolder(holder: ChatRowVH, position: Int) {
        val chat = chats[position]
        
        // Find the other participant's ID
        val otherUserId = chat.participants.find { it != currentUserId } ?: ""
        
        holder.tvName.text = chat.userNames[otherUserId] ?: "User"
        holder.tvLastMsg.text = chat.lastMessage
        
        val date = chat.lastTimestamp?.toDate()
        if (date != null) {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            holder.tvTime.text = sdf.format(date)
        } else {
            holder.tvTime.text = ""
        }

        holder.itemView.setOnClickListener { onClick(chat) }
    }

    override fun getItemCount() = chats.size

    fun updateList(newList: List<Chat>) {
        chats = newList
        notifyDataSetChanged()
    }
}

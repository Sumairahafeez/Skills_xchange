package com.example.skillsexchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatListAdapter(
    private val users: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatRowVH>() {

    inner class ChatRowVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvChatUserName)
        val tvTagline: TextView = view.findViewById(R.id.tvChatUserTagline)
        val tvLastMsg: TextView = view.findViewById(R.id.tvLastMessage)
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

        // Last message dikhao cache se
        val messages = ChatCache.load(holder.itemView.context, user.id)
        holder.tvLastMsg.text = if (messages.isNotEmpty())
            messages.last().text.take(40) + if (messages.last().text.length > 40) "..." else ""
        else ""

        holder.itemView.setOnClickListener { onClick(user) }
    }

    override fun getItemCount() = users.size
}
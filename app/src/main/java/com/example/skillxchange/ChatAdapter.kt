package com.example.skillxchange

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvChatBubble)
        val container: LinearLayout = itemView.findViewById(R.id.chatBubbleContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.tvMessage.text = message.text
        
        val isMe = message.senderId == currentUserId
        val params = holder.tvMessage.layoutParams as LinearLayout.LayoutParams
        
        if (isMe) {
            holder.container.gravity = Gravity.END
            holder.tvMessage.setBackgroundResource(R.drawable.bg_bubble_sent)
            holder.tvMessage.setTextColor(android.graphics.Color.WHITE)
            params.gravity = Gravity.END
        } else {
            holder.container.gravity = Gravity.START
            holder.tvMessage.setBackgroundResource(R.drawable.bg_bubble_received)
            holder.tvMessage.setTextColor(android.graphics.Color.BLACK)
            params.gravity = Gravity.START
        }
        holder.tvMessage.layoutParams = params
    }

    override fun getItemCount(): Int = messages.size
}

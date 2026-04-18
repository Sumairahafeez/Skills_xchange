package com.example.skillsexchange

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatVH>() {

    inner class ChatVH(view: View) : RecyclerView.ViewHolder(view) {
        val bubble: TextView = view.findViewById(R.id.tvChatBubble)
        val container: LinearLayout = view.findViewById(R.id.chatBubbleContainer)
        val cardSharedPost: MaterialCardView = view.findViewById(R.id.cardSharedPost)
        val tvSharedAuthor: TextView = view.findViewById(R.id.tvSharedPostAuthor)
        val tvSharedContent: TextView = view.findViewById(R.id.tvSharedPostContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatVH(view)
    }

    override fun onBindViewHolder(holder: ChatVH, position: Int) {
        val msg = messages[position]
        
        // Handle alignment
        val params = holder.bubble.layoutParams as LinearLayout.LayoutParams
        val cardParams = holder.cardSharedPost.layoutParams as LinearLayout.LayoutParams
        
        if (msg.isSentByMe) {
            holder.container.gravity = Gravity.END
            holder.bubble.setBackgroundResource(R.drawable.bg_bubble_sent)
            holder.bubble.setTextColor(holder.bubble.context.getColor(android.R.color.white))
            params.gravity = Gravity.END
            cardParams.gravity = Gravity.END
        } else {
            holder.container.gravity = Gravity.START
            holder.bubble.setBackgroundResource(R.drawable.bg_bubble_received)
            holder.bubble.setTextColor(holder.bubble.context.getColor(R.color.color_text_primary))
            params.gravity = Gravity.START
            cardParams.gravity = Gravity.START
        }
        
        holder.bubble.layoutParams = params
        holder.cardSharedPost.layoutParams = cardParams

        // Show text or post
        if (msg.sharedPostId != null) {
            holder.bubble.visibility = View.GONE
            holder.cardSharedPost.visibility = View.VISIBLE
            
            // Find post data
            val post = CreatePostActivity.sharedPosts.find { it.id == msg.sharedPostId }
            if (post != null) {
                holder.tvSharedAuthor.text = post.userName
                holder.tvSharedContent.text = post.content
                
                holder.cardSharedPost.setOnClickListener {
                    Toast.makeText(holder.itemView.context, "Opening post: ${post.id}", Toast.LENGTH_SHORT).show()
                    // In a real app, you'd navigate to PostDetailActivity here
                }
            } else {
                holder.tvSharedAuthor.text = "Unknown Post"
                holder.tvSharedContent.text = msg.text
            }
        } else {
            holder.bubble.visibility = View.VISIBLE
            holder.cardSharedPost.visibility = View.GONE
            holder.bubble.text = msg.text
        }
    }

    override fun getItemCount() = messages.size

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }
}
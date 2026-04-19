package com.example.skillxchange

import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
        return try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_row, parent, false)
            ChatRowVH(view)
        } catch (e: Exception) {
            Log.e("ChatListAdapter", "Error creating ViewHolder", e)
            // Return a dummy view to avoid immediate crash
            ChatRowVH(View(parent.context))
        }
    }

    override fun onBindViewHolder(holder: ChatRowVH, position: Int) {
        try {
            val user = users[position]
            holder.tvName.text = user.name
            holder.tvTagline.text = user.tagline

            val context = holder.itemView.context
            val prefs = context.getSharedPreferences("skillsxchange_prefs", Context.MODE_PRIVATE)
            val currentUserId = prefs.getString("user_id", "") ?: ""

            val messages = try {
                ChatCache.load(context, currentUserId, user.id)
            } catch (e: Exception) {
                Log.e("ChatListAdapter", "Error loading cache", e)
                mutableListOf()
            }
            
            val lastMsg = messages.lastOrNull()
            
            holder.tvLastMsg.text = if (lastMsg != null)
                lastMsg.text.take(40) + if (lastMsg.text.length > 40) "..." else ""
            else ""

            val isViewed = try {
                ChatCache.isViewed(context, currentUserId, user.id)
            } catch (e: Exception) {
                true
            }
            
            if (!isViewed && lastMsg != null && lastMsg.senderId != currentUserId) {
                holder.tvName.setTypeface(null, Typeface.BOLD)
                holder.tvLastMsg.setTypeface(null, Typeface.BOLD)
                try {
                    holder.tvLastMsg.setTextColor(context.getColor(R.color.color_text_primary))
                } catch (e: Exception) {
                    holder.tvLastMsg.setTextColor(android.graphics.Color.BLACK)
                }
                holder.dot.visibility = View.VISIBLE
            } else {
                holder.tvName.setTypeface(null, Typeface.NORMAL)
                holder.tvLastMsg.setTypeface(null, Typeface.NORMAL)
                try {
                    holder.tvLastMsg.setTextColor(context.getColor(R.color.color_text_secondary))
                } catch (e: Exception) {
                    holder.tvLastMsg.setTextColor(android.graphics.Color.GRAY)
                }
                holder.dot.visibility = View.GONE
            }

            holder.itemView.setOnClickListener { 
                try {
                    ChatCache.markViewed(context, currentUserId, user.id, true)
                    onClick(user)
                } catch (e: Exception) {
                    Log.e("ChatListAdapter", "Error in click listener", e)
                    Toast.makeText(context, "Error opening chat", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("ChatListAdapter", "Error binding view holder", e)
        }
    }

    override fun getItemCount() = users.size

    fun updateList(newList: List<User>) {
        try {
            users = newList
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("ChatListAdapter", "Error updating list", e)
        }
    }
}

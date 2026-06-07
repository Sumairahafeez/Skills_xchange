package com.example.skillxchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skillxchange.model.Notification
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(
    private var notificationList: List<Notification>,
    private val onNotificationClicked: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivSender: ShapeableImageView = itemView.findViewById(R.id.ivNotificationSender)
        val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        val viewUnreadDot: View = itemView.findViewById(R.id.viewUnreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]

        // Combine name and message for better clarity
        val fullMessage = "${notification.fromUserName} ${notification.message}"
        holder.tvMessage.text = fullMessage
        
        val date = notification.timestamp?.toDate()
        if (date != null) {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            holder.tvTime.text = sdf.format(date)
        } else {
            holder.tvTime.text = "Just now"
        }

        Glide.with(holder.itemView.context)
            .load(notification.fromUserProfileUrl)
            .placeholder(R.drawable.ic_user_placeholder)
            .into(holder.ivSender)

        holder.viewUnreadDot.visibility = if (notification.isRead) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener {
            onNotificationClicked(notification)
        }
    }

    override fun getItemCount(): Int = notificationList.size

    fun updateNotifications(newList: List<Notification>) {
        notificationList = newList
        notifyDataSetChanged()
    }
}

package com.example.skillxchange

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.skillxchange.model.Notification
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

object NotificationHelper {
    private const val CHANNEL_ID = "skillxchange_notifications"
    private const val CHANNEL_NAME = "SkillXchange Alerts"

    fun createNotification(
        toUserId: String,
        fromUserId: String,
        fromUserName: String,
        fromUserProfileUrl: String,
        message: String,
        type: String,
        relatedId: String
    ) {
        if (toUserId == fromUserId || toUserId.isEmpty()) return 

        val db = FirebaseFirestore.getInstance()
        val id = UUID.randomUUID().toString()
        val notification = Notification(
            id = id,
            toUserId = toUserId,
            fromUserId = fromUserId,
            fromUserName = fromUserName.ifEmpty { "Someone" },
            fromUserProfileUrl = fromUserProfileUrl,
            message = message,
            type = type,
            relatedId = relatedId,
            timestamp = Timestamp.now(),
            isRead = false
        )

        db.collection("notifications").document(id).set(notification)
    }

    /**
     * Shows a local system notification on the phone.
     */
    fun showSystemNotification(context: Context, notification: Notification) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, NotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(notification.fromUserName)
            .setContentText(notification.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notification.id.hashCode(), builder.build())
    }
}

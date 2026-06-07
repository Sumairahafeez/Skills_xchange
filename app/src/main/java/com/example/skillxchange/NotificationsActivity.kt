package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.Notification
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvNotifications = findViewById<RecyclerView>(R.id.rvNotifications)
        notificationAdapter = NotificationAdapter(emptyList()) { notification ->
            markAsRead(notification)
            handleNotificationClick(notification)
        }

        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = notificationAdapter

        listenForNotifications()
    }

    private fun listenForNotifications() {
        val uid = auth.currentUser?.uid ?: return
        
        Log.d("NotificationsActivity", "Listening for notifications for UID: $uid")
        
        // Fetch notifications for the current user
        db.collection("notifications")
            .whereEqualTo("toUserId", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("NotificationsActivity", "Error loading notifications", e)
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val list = snapshot.toObjects(Notification::class.java)
                    Log.d("NotificationsActivity", "Received ${list.size} notifications")
                    
                    // Sort in-memory by timestamp (most recent first)
                    // We handle null timestamps (pending sync) by putting them at the top
                    val sortedList = list.sortedWith(compareByDescending<Notification> { 
                        it.timestamp?.seconds ?: Long.MAX_VALUE 
                    }.thenByDescending { it.timestamp?.nanoseconds ?: Long.MAX_VALUE })
                    
                    notificationAdapter.updateNotifications(sortedList)
                }
            }
    }

    private fun markAsRead(notification: Notification) {
        if (!notification.isRead) {
            db.collection("notifications").document(notification.id)
                .update("isRead", true)
                .addOnFailureListener { e ->
                    Log.e("NotificationsActivity", "Failed to mark as read", e)
                }
        }
    }

    private fun handleNotificationClick(notification: Notification) {
        when (notification.type) {
            "CONNECTION_REQUEST", "CONNECTION_ACCEPT", "LIKE", "COMMENT" -> {
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("userId", notification.fromUserId)
                }
                startActivity(intent)
            }
            "MESSAGE" -> {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", notification.fromUserId)
                    putExtra("userName", notification.fromUserName)
                }
                startActivity(intent)
            }
        }
    }
}

package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ConnectActivity : AppCompatActivity() {

    private lateinit var rvInvitations: RecyclerView
    private lateinit var invitationAdapter: InvitationAdapter
    private var invitationsList = mutableListOf<Invitation>()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarNetwork)
        toolbar.setNavigationOnClickListener { finish() }

        rvInvitations = findViewById(R.id.rvInvitations)
        val tvInvitationsCount = findViewById<TextView>(R.id.tvInvitationsCount)
        val layoutInvitations = findViewById<LinearLayout>(R.id.layoutInvitations)

        invitationAdapter = InvitationAdapter(invitationsList,
            onAccept = { invitation ->
                handleAccept(invitation)
            },
            onDecline = { invitation, position ->
                handleDecline(invitation, position)
            }
        )
        rvInvitations.layoutManager = LinearLayoutManager(this)
        rvInvitations.adapter = invitationAdapter

        val rvSuggestions = findViewById<RecyclerView>(R.id.rvSuggestions)
        val suggestionsList = mutableListOf<User>()
        val suggestionAdapter = SuggestionAdapter(suggestionsList) { user ->
            sendConnectionRequest(user)
        }
        rvSuggestions.layoutManager = GridLayoutManager(this, 2)
        rvSuggestions.adapter = suggestionAdapter

        fetchInvitations(tvInvitationsCount, layoutInvitations)
        fetchSuggestions(suggestionsList, suggestionAdapter)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationNetwork)
        bottomNavigation.selectedItemId = R.id.nav_connections
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); finish(); true }
                R.id.nav_connections -> true
                R.id.nav_messages -> { startActivity(Intent(this, ChatListActivity::class.java)); finish(); true }
                R.id.nav_create_post -> { startActivity(Intent(this, CreatePostActivity::class.java)); finish(); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); finish(); true }
                else -> false
            }
        }
    }

    private fun fetchInvitations(tvCount: TextView, layout: View) {
        db.collection("connections")
            .whereEqualTo("status", "pending")
            .whereArrayContains("users", currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val pendingUids = snapshot.documents.filter { doc ->
                        doc.getString("requestSenderId") != currentUserId
                    }.mapNotNull { doc ->
                        val users = doc.get("users") as? List<String>
                        users?.find { it != currentUserId }
                    }

                    if (pendingUids.isEmpty()) {
                        invitationsList.clear()
                        invitationAdapter.notifyDataSetChanged()
                        tvCount.text = "Invitations (0)"
                        layout.visibility = View.GONE
                    } else {
                        db.collection("users").whereIn("uid", pendingUids).get().addOnSuccessListener { userSnap ->
                            invitationsList.clear()
                            val users = userSnap.toObjects(User::class.java)
                            invitationsList.addAll(users.map {
                                Invitation(id = it.uid, name = it.name, title = it.tagline, reason = "Wants to connect")
                            })
                            invitationAdapter.notifyDataSetChanged()
                            tvCount.text = "Invitations (${invitationsList.size})"
                            layout.visibility = View.VISIBLE
                        }
                    }
                }
            }
    }

    private fun fetchSuggestions(list: MutableList<User>, adapter: SuggestionAdapter) {
        // Simple suggestion: Fetch all users except me and those I'm connected to
        db.collection("users").limit(20).get().addOnSuccessListener { snapshot ->
            val allUsers = snapshot.toObjects(User::class.java)
            // Filter out self and existing connections (simplified for now)
            db.collection("connections").whereArrayContains("users", currentUserId).get().addOnSuccessListener { connSnap ->
                val connectedUids = connSnap.documents.flatMap { doc ->
                    (doc.get("users") as? List<String>) ?: emptyList()
                }.toSet()

                list.clear()
                list.addAll(allUsers.filter { it.uid != currentUserId && !connectedUids.contains(it.uid) })
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun handleAccept(invitation: Invitation) {
        val connectionId = if (currentUserId < invitation.id) "${currentUserId}_${invitation.id}" else "${invitation.id}_${currentUserId}"
        db.collection("connections").document(connectionId)
            .update("status", "accepted", "updatedAt", FieldValue.serverTimestamp())
            .addOnSuccessListener {
                Toast.makeText(this, "Connection accepted!", Toast.LENGTH_SHORT).show()
                // Send Notification
                db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
                    val me = doc.toObject(User::class.java)
                    NotificationHelper.createNotification(
                        toUserId = invitation.id,
                        fromUserId = currentUserId,
                        fromUserName = me?.name ?: "Someone",
                        fromUserProfileUrl = me?.photoUrl ?: "",
                        message = "accepted your connection request",
                        type = "CONNECTION",
                        relatedId = connectionId
                    )
                }
            }
    }

    private fun handleDecline(invitation: Invitation, position: Int) {
        val connectionId = if (currentUserId < invitation.id) "${currentUserId}_${invitation.id}" else "${invitation.id}_${currentUserId}"
        db.collection("connections").document(connectionId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Request declined", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendConnectionRequest(user: User) {
        val connectionId = if (currentUserId < user.uid) "${currentUserId}_${user.uid}" else "${user.uid}_${currentUserId}"
        val connectionData = hashMapOf(
            "id" to connectionId,
            "users" to listOf(currentUserId, user.uid),
            "status" to "pending",
            "requestSenderId" to currentUserId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("connections").document(connectionId).set(connectionData)
            .addOnSuccessListener {
                Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
                // Notify
                db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
                    val me = doc.toObject(User::class.java)
                    NotificationHelper.createNotification(
                        toUserId = user.uid,
                        fromUserId = currentUserId,
                        fromUserName = me?.name ?: "Someone",
                        fromUserProfileUrl = me?.photoUrl ?: "",
                        message = "sent you a connection request",
                        type = "CONNECTION_REQUEST",
                        relatedId = connectionId
                    )
                }
            }
    }
}

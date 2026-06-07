package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MatchResultActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UserAdapter
    private val matchedUsersList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_result)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.matchToolbar)
        val tvMatchTitle = findViewById<TextView>(R.id.tvMatchTitle)
        val rvMatches = findViewById<RecyclerView>(R.id.rvMatches)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar) // Make sure to add a ProgressBar to your XML layout if you want to show loading states

        toolbar.setNavigationOnClickListener { finish() }

        val userName = intent.getStringExtra("userName") ?: "you"
        tvMatchTitle.text = "People matching with $userName"

        val currentUserId = auth.currentUser?.uid ?: ""

        // Set up adapter with click handlers mapping perfectly to your model schemas
        adapter = UserAdapter(
            userList = matchedUsersList,
            connectionInfo = emptyMap(),
            currentUserId = currentUserId,
            onConnectClick = { peer -> handleConnectRequest(peer) },
            onAcceptClick = { /* Handled automatically inside Connection lists */ },
            onChatClick = { user ->
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", user.uid)
                    putExtra("userName", user.name)
                }
                startActivity(intent)
            },
            onProfileClick = { user ->
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("userId", user.uid)
                }
                startActivity(intent)
            }
        )

        rvMatches.layoutManager = LinearLayoutManager(this)
        rvMatches.adapter = adapter

        // Step 1: Read the current user's direct learning and teaching needs dynamically
        if (currentUserId.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE
            db.collection("users").document(currentUserId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val currentUserModel = documentSnapshot.toObject(User::class.java)
                    if (currentUserModel != null) {
                        // Step 2: Fire matching pipeline using live skills requirements
                        fetchLiveMutualMatches(currentUserModel, progressBar)
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Profile configuration not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to load skills: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchLiveMutualMatches(currentUser: User, progressBar: ProgressBar) {
        // Guard checking if the user hasn't added what they wish to learn yet
        if (currentUser.learnSkills.isEmpty()) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Please add skills you want to learn first!", Toast.LENGTH_LONG).show()
            return
        }

        // Server-Side: Filter for users who teach what you want to learn
        db.collection("users")
            .whereArrayContainsAny("teachSkills", currentUser.learnSkills)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE

                if (error != null) {
                    Log.e("MatchResultActivity", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    matchedUsersList.clear()

                    val parsedPeers = snapshot.documents.mapNotNull { doc ->
                        val peer = doc.toObject(User::class.java)
                        val uid = if (peer?.uid.isNullOrEmpty()) doc.id else peer!!.uid
                        peer?.copy(uid = uid)
                    }

                    // Client-Side Optimization: Cross-reference who wants to learn what you teach
                    val verifiedMatches = parsedPeers.filter { peer ->
                        peer.uid != currentUser.uid &&
                                peer.learnSkills.any { it in currentUser.teachSkills }
                    }

                    matchedUsersList.addAll(verifiedMatches)
                    adapter.notifyDataSetChanged()

                    if (matchedUsersList.isEmpty()) {
                        Toast.makeText(this, "No mutual skill matches found right now.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun handleConnectRequest(targetUser: User) {
        val currentUserId = auth.currentUser?.uid ?: return
        val connectionId = if (currentUserId < targetUser.uid) "${currentUserId}_${targetUser.uid}" else "${targetUser.uid}_${currentUserId}"

        val connectionData = mapOf(
            "id" to connectionId,
            "users" to listOf(currentUserId, targetUser.uid),
            "status" to "pending",
            "requestSenderId" to currentUserId,
            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("connections").document(connectionId)
            .set(connectionData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Connection request sent to ${targetUser.name}!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to connect: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ExploreActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var etSearch: TextInputEditText
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private var allUsers = listOf<User>()
    private var filteredUsers = listOf<User>()
    private var connectionInfoMap = mutableMapOf<String, ConnectionInfo>()
    
    private var currentUserProfile: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUserId = auth.currentUser?.uid ?: ""

        rvUsers = findViewById(R.id.rvExploreUsers)
        etSearch = findViewById(R.id.etSearchSkill)

        userAdapter = UserAdapter(
            userList = emptyList(),
            connectionInfo = connectionInfoMap,
            currentUserId = currentUserId,
            onConnectClick = { user -> handleConnect(user) },
            onAcceptClick = { user -> handleAccept(user) },
            onChatClick = { user ->
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", user.uid)
                    putExtra("userName", user.name)
                }
                startActivity(intent)
            },
            onProfileClick = { user -> 
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("userId", user.uid)
                startActivity(intent)
            }
        )

        rvUsers.layoutManager = GridLayoutManager(this, 2)
        rvUsers.adapter = userAdapter

        fetchMyProfile()
        listenForUsers(currentUserId)
        listenForConnections(currentUserId)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<BottomNavigationView>(R.id.bottomNavigation).apply {
            selectedItemId = R.id.nav_connections
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> { startActivity(Intent(this@ExploreActivity, HomeActivity::class.java)); finish(); true }
                    R.id.nav_connections -> true
                    R.id.nav_create_post -> { startActivity(Intent(this@ExploreActivity, CreatePostActivity::class.java)); finish(); true }
                    R.id.nav_messages -> { startActivity(Intent(this@ExploreActivity, ChatListActivity::class.java)); finish(); true }
                    R.id.nav_profile -> { startActivity(Intent(this@ExploreActivity, ProfileActivity::class.java)); finish(); true }
                    else -> false
                }
            }
        }
    }

    private fun fetchMyProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener {
            currentUserProfile = it.toObject(User::class.java)
        }
    }

    private fun listenForUsers(currentUserId: String) {
        db.collection("users").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                allUsers = snapshot.documents.mapNotNull { doc ->
                    val user = doc.toObject(User::class.java)
                    val uid = if (user?.uid.isNullOrEmpty()) doc.id else user!!.uid
                    user?.copy(uid = uid)
                }.filter { it.uid != currentUserId }
                filterUsers(etSearch.text.toString())
            }
        }
    }

    private fun listenForConnections(currentUserId: String) {
        db.collection("connections")
            .whereArrayContains("users", currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    connectionInfoMap.clear()
                    for (doc in snapshot.documents) {
                        val users = doc.get("users") as? List<String> ?: continue
                        val status = doc.getString("status") ?: ""
                        val senderId = doc.getString("requestSenderId") ?: ""
                        val otherId = users.find { it != currentUserId } ?: continue
                        connectionInfoMap[otherId] = ConnectionInfo(status, senderId)
                    }
                    userAdapter.updateData(filteredUsers, connectionInfoMap)
                }
            }
    }

    private fun filterUsers(query: String) {
        filteredUsers = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true) ||
                user.skills.any { it.contains(query, ignoreCase = true) } ||
                user.tagline.contains(query, ignoreCase = true)
            }
        }
        userAdapter.updateData(filteredUsers, connectionInfoMap)
    }

    private fun handleConnect(targetUser: User) {
        val currentUserId = auth.currentUser?.uid ?: return
        val connectionId = if (currentUserId < targetUser.uid) "${currentUserId}_${targetUser.uid}" else "${targetUser.uid}_${currentUserId}"
        val connectionData = mapOf(
            "id" to connectionId,
            "users" to listOf(currentUserId, targetUser.uid),
            "status" to "pending",
            "requestSenderId" to currentUserId,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("connections").document(connectionId).set(connectionData)
            .addOnSuccessListener {
                Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
                NotificationHelper.createNotification(
                    toUserId = targetUser.uid,
                    fromUserId = currentUserId,
                    fromUserName = currentUserProfile?.name ?: auth.currentUser?.displayName ?: "Someone",
                    fromUserProfileUrl = currentUserProfile?.photoUrl ?: "",
                    message = "sent you a connection request",
                    type = "CONNECTION_REQUEST",
                    relatedId = connectionId
                )
            }
    }

    private fun handleAccept(targetUser: User) {
        val currentUserId = auth.currentUser?.uid ?: return
        val connectionId = if (currentUserId < targetUser.uid) "${currentUserId}_${targetUser.uid}" else "${targetUser.uid}_${currentUserId}"
        
        db.runBatch { batch ->
            batch.update(db.collection("connections").document(connectionId), 
                "status", "accepted", "updatedAt", FieldValue.serverTimestamp())
            
            batch.update(db.collection("users").document(currentUserId), "connections", FieldValue.arrayUnion(targetUser.uid))
            batch.update(db.collection("users").document(targetUser.uid), "connections", FieldValue.arrayUnion(currentUserId))
        }.addOnSuccessListener {
            Toast.makeText(this, "Connection accepted!", Toast.LENGTH_SHORT).show()
            NotificationHelper.createNotification(
                toUserId = targetUser.uid,
                fromUserId = currentUserId,
                fromUserName = currentUserProfile?.name ?: auth.currentUser?.displayName ?: "Someone",
                fromUserProfileUrl = currentUserProfile?.photoUrl ?: "",
                message = "accepted your connection request",
                type = "CONNECTION_ACCEPT",
                relatedId = connectionId
            )
        }
    }
}

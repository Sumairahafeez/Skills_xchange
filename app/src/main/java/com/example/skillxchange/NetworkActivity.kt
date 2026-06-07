package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NetworkActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: NetworkAdapter
    private lateinit var rvNetwork: RecyclerView
    
    private var currentUserId: String = ""
    private var isPendingTab = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        rvNetwork = findViewById(R.id.rvNetwork)

        adapter = NetworkAdapter(
            users = emptyList(),
            isPendingTab = false,
            onAccept = { user -> handleAccept(user) },
            onDecline = { user -> handleDecline(user) },
            onMessage = { user -> 
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", user.uid)
                    putExtra("userName", user.name)
                }
                startActivity(intent)
            }
        )

        rvNetwork.layoutManager = LinearLayoutManager(this)
        rvNetwork.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isPendingTab = tab?.position == 1
                fetchData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        fetchData()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
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

    private fun fetchData() {
        if (isPendingTab) {
            fetchPendingRequests()
        } else {
            fetchAcceptedConnections()
        }
    }

    private fun fetchAcceptedConnections() {
        db.collection("connections")
            .whereArrayContains("users", currentUserId)
            .whereEqualTo("status", "accepted")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val uids = snapshot.documents.mapNotNull { doc ->
                        val users = doc.get("users") as? List<String>
                        users?.find { it != currentUserId }
                    }
                    if (uids.isEmpty()) {
                        adapter.updateData(emptyList(), false)
                    } else {
                        fetchUsersByUids(uids, false)
                    }
                }
            }
    }

    private fun fetchPendingRequests() {
        db.collection("connections")
            .whereEqualTo("status", "pending")
            .whereArrayContains("users", currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val uids = snapshot.documents.filter { doc ->
                        doc.getString("requestSenderId") != currentUserId
                    }.mapNotNull { doc ->
                        val users = doc.get("users") as? List<String>
                        users?.find { it != currentUserId }
                    }
                    if (uids.isEmpty()) {
                        adapter.updateData(emptyList(), true)
                    } else {
                        fetchUsersByUids(uids, true)
                    }
                }
            }
    }

    private fun fetchUsersByUids(uids: List<String>, isPending: Boolean) {
        // Firestore 'in' query supports up to 10-30 elements depending on version, 
        // for simplicity assuming small number of pending/connections or handling first 10
        db.collection("users").whereIn("uid", uids.take(30)).get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.toObjects(User::class.java)
                adapter.updateData(users, isPending)
            }
    }

    private fun handleAccept(user: User) {
        val connectionId = if (currentUserId < user.uid) "${currentUserId}_${user.uid}" else "${user.uid}_${currentUserId}"
        db.collection("connections").document(connectionId)
            .update("status", "accepted", "updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())
            .addOnSuccessListener {
                Toast.makeText(this, "Connection Accepted", Toast.LENGTH_SHORT).show()
                // Notify
                db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
                    val me = doc.toObject(User::class.java)
                    NotificationHelper.createNotification(
                        toUserId = user.uid,
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

    private fun handleDecline(user: User) {
        val connectionId = if (currentUserId < user.uid) "${currentUserId}_${user.uid}" else "${user.uid}_${currentUserId}"
        db.collection("connections").document(connectionId).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Request Declined", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skillxchange.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewersActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UserAdapter
    private val viewersList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_viewers)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.viewersToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val rvViewers = findViewById<RecyclerView>(R.id.rvViewers)
        val tvNoViewers = findViewById<TextView>(R.id.tvNoViewers)

        adapter = UserAdapter(
            userList = viewersList,
            connectionInfo = emptyMap(),
            currentUserId = auth.currentUser?.uid ?: "",
            onConnectClick = { /* Handled in Explore */ },
            onAcceptClick = { /* Handled in Explore */ },
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

        rvViewers.layoutManager = LinearLayoutManager(this)
        rvViewers.adapter = adapter

        fetchViewers(tvNoViewers)
    }

    private fun fetchViewers(emptyState: TextView) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            if (isFinishing || isDestroyed) return@addOnSuccessListener
            
            // Get viewer IDs and remove duplicates
            val viewerIds = (snapshot.get("viewedBy") as? List<*>)
                ?.mapNotNull { it?.toString() }
                ?.filter { it.isNotEmpty() }
                ?.distinct() ?: emptyList()
            
            if (viewerIds.isEmpty()) {
                emptyState.visibility = View.VISIBLE
                viewersList.clear()
                adapter.updateData(viewersList, emptyMap())
                return@addOnSuccessListener
            }

            // Firestore 'whereIn' supports up to 30 elements. 
            // We'll take the 30 most recent viewers.
            val limitedIds = viewerIds.takeLast(30)

            db.collection("users")
                .whereIn(FieldPath.documentId(), limitedIds)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (isFinishing || isDestroyed) return@addOnSuccessListener
                    
                    val users = querySnapshot.toObjects(User::class.java)
                    
                    // Sort the users to match the order in viewerIds (most recent first)
                    val sortedUsers = users.sortedByDescending { user -> 
                        viewerIds.indexOf(user.uid) 
                    }
                    
                    viewersList.clear()
                    viewersList.addAll(sortedUsers)
                    adapter.updateData(viewersList, emptyMap())
                    
                    emptyState.visibility = if (viewersList.isEmpty()) View.VISIBLE else View.GONE
                }
                .addOnFailureListener {
                    if (!isFinishing && !isDestroyed) {
                        emptyState.text = "Error loading viewers"
                        emptyState.visibility = View.VISIBLE
                    }
                }
        }.addOnFailureListener {
            if (!isFinishing && !isDestroyed) {
                emptyState.text = "Error fetching data"
                emptyState.visibility = View.VISIBLE
            }
        }
    }
}

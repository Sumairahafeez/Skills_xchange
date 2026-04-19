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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class ConnectActivity : AppCompatActivity() {

    private lateinit var rvInvitations: RecyclerView
    private lateinit var invitationAdapter: InvitationAdapter
    private var invitations = mutableListOf<Invitation>()
    private lateinit var currentUserId: String
    private lateinit var currentUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        val prefs = getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE)
        currentUserId = prefs.getString("user_id", "") ?: ""
        currentUserName = prefs.getString("user_name", "Someone") ?: "Someone"

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarNetwork)
        toolbar.setNavigationOnClickListener { finish() }

        rvInvitations = findViewById(R.id.rvInvitations)
        val tvInvitationsCount = findViewById<TextView>(R.id.tvInvitationsCount)
        val layoutInvitations = findViewById<LinearLayout>(R.id.layoutInvitations)

        invitations = ConnectionCache.getInvitationsForUser(this, currentUserId).toMutableList()
        updateInvitationHeader(tvInvitationsCount)
        
        layoutInvitations.visibility = if (invitations.isEmpty()) View.GONE else View.VISIBLE

        invitationAdapter = InvitationAdapter(invitations, 
            onAccept = { invitation ->
                Toast.makeText(this, "Accepted invitation from ${invitation.name}", Toast.LENGTH_SHORT).show()
                ConnectionCache.acceptConnection(this, currentUserId, invitation.id)
                ConnectionCache.removeInvitation(this, currentUserId, invitation.id)
                
                refreshInvitations(tvInvitationsCount, layoutInvitations)
                
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("userId", invitation.id)
                    putExtra("userName", invitation.name)
                }
                startActivity(intent)
            },
            onDecline = { invitation, position ->
                ConnectionCache.removeInvitation(this, currentUserId, invitation.id)
                invitations.removeAt(position)
                invitationAdapter.notifyItemRemoved(position)
                updateInvitationHeader(tvInvitationsCount)
                if (invitations.isEmpty()) {
                    layoutInvitations.visibility = View.GONE
                }
            }
        )
        rvInvitations.layoutManager = LinearLayoutManager(this)
        rvInvitations.adapter = invitationAdapter

        val rvSuggestions = findViewById<RecyclerView>(R.id.rvSuggestions)
        val allUsers = UserCache.getAllUsers(this)
        val friends = ConnectionCache.getFriendsForUser(this, currentUserId)
        
        // Filter: Not me, and not already friends
        val suggestions = allUsers.filter { it.id != currentUserId && !friends.contains(it.id) }

        rvSuggestions.layoutManager = GridLayoutManager(this, 2)
        rvSuggestions.adapter = SuggestionAdapter(suggestions) { user ->
            // Send invitation TO the other user FROM me
            val currentUserData = UserCache.getAllUsers(this).find { it.id == currentUserId }
            val newInv = Invitation(
                id = currentUserId, 
                name = currentUserName, 
                title = currentUserData?.tagline ?: "Member", 
                reason = "Wants to connect"
            )
            ConnectionCache.sendInvitation(this, user.id, newInv)
            Toast.makeText(this, "Connection request sent to ${user.name}", Toast.LENGTH_SHORT).show()
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationNetwork)
        bottomNavigation.selectedItemId = R.id.nav_connections
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); finish(); true }
                R.id.nav_connections -> true
                R.id.nav_messages -> { startActivity(Intent(this, ChatListActivity::class.java)); true }
                R.id.nav_create_post -> { startActivity(Intent(this, CreatePostActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val tvInvitationsCount = findViewById<TextView>(R.id.tvInvitationsCount)
        val layoutInvitations = findViewById<LinearLayout>(R.id.layoutInvitations)
        refreshInvitations(tvInvitationsCount, layoutInvitations)
    }

    private fun refreshInvitations(tvCount: TextView, layout: View) {
        invitations.clear()
        invitations.addAll(ConnectionCache.getInvitationsForUser(this, currentUserId))
        invitationAdapter.notifyDataSetChanged()
        updateInvitationHeader(tvCount)
        layout.visibility = if (invitations.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun updateInvitationHeader(tvCount: TextView) {
        tvCount.text = "Invitations (${invitations.size})"
    }
}

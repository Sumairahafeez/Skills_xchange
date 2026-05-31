package com.example.skillxchange

import android.content.Context
import com.google.firebase.database.*

object ConnectionCache {
    private val database = FirebaseDatabase.getInstance().reference

    fun sendInvitation(context: Context, targetUserId: String, senderInvitation: Invitation) {
        database.child("invitations").child(targetUserId).child(senderInvitation.id).setValue(senderInvitation)
    }

    fun listenToInvitations(userId: String, callback: (List<Invitation>) -> Unit) {
        database.child("invitations").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Invitation>()
                for (child in snapshot.children) {
                    child.getValue(Invitation::class.java)?.let { list.add(it) }
                }
                callback(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun removeInvitation(context: Context, currentUserId: String, senderId: String) {
        database.child("invitations").child(currentUserId).child(senderId).removeValue()
    }

    fun acceptConnection(context: Context, currentUserId: String, friendId: String) {
        database.child("friends").child(currentUserId).child(friendId).setValue(true)
        database.child("friends").child(friendId).child(currentUserId).setValue(true)
        removeInvitation(context, currentUserId, friendId)
    }

    fun listenToFriends(userId: String, callback: (Set<String>) -> Unit) {
        database.child("friends").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val set = mutableSetOf<String>()
                for (child in snapshot.children) {
                    set.add(child.key ?: "")
                }
                callback(set)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Legacy sync methods for immediate use (though empty/stubs as we should use listeners)
    fun getInvitationsForUser(context: Context, userId: String): List<Invitation> = emptyList()
    fun getFriendsForUser(context: Context, userId: String): Set<String> = emptySet()
    fun clearAllData(context: Context) {}
}

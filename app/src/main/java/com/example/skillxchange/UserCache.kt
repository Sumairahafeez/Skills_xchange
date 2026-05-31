package com.example.skillxchange

import android.content.Context
import com.google.firebase.database.*

object UserCache {
    private val database = FirebaseDatabase.getInstance().getReference("users")
    private var cachedUsers = mutableListOf<User>()

    init {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<User>()
                for (child in snapshot.children) {
                    child.getValue(User::class.java)?.let { list.add(it) }
                }
                cachedUsers = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun saveUser(context: Context, user: User) {
        database.child(user.id).setValue(user)
    }

    fun getAllUsers(context: Context): List<User> {
        return cachedUsers
    }

    fun clearData(context: Context) {
        // Firebase data is global, but we can clear local if we had any
    }
    
    fun listenToUsers(callback: (List<User>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<User>()
                for (child in snapshot.children) {
                    child.getValue(User::class.java)?.let { list.add(it) }
                }
                callback(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

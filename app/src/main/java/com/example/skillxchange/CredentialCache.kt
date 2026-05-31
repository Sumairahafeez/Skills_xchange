package com.example.skillxchange

import android.content.Context
import com.google.firebase.database.*

object CredentialCache {
    private val database = FirebaseDatabase.getInstance().getReference("credentials")
    private var cachedCredentials = mutableListOf<Credential>()

    data class Credential(val userId: String = "", val email: String = "", val password: String = "", val name: String = "")

    init {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Credential>()
                for (child in snapshot.children) {
                    child.getValue(Credential::class.java)?.let { list.add(it) }
                }
                cachedCredentials = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun saveCredential(context: Context, cred: Credential) {
        // Use encoded email as key because Firebase keys can't contain '.'
        val encodedEmail = cred.email.replace(".", ",")
        database.child(encodedEmail).setValue(cred)
    }

    fun getAllCredentials(context: Context): List<Credential> {
        return cachedCredentials
    }

    fun getCredential(context: Context, email: String, callback: (Credential?) -> Unit) {
        val encodedEmail = email.replace(".", ",")
        database.child(encodedEmail).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.getValue(Credential::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    fun clearData(context: Context) {
        // Global data, usually not cleared from client like this
    }
}

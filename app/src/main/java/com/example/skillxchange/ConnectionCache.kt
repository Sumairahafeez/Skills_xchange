package com.example.skillxchange

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object ConnectionCache {
    private const val TAG = "ConnectionCache"
    private const val PREFS_NAME = "connection_storage"
    private const val KEY_INVITATIONS = "pending_invitations" // Map: targetUserId -> List of Invitations
    private const val KEY_FRIENDS = "accepted_connections" // Map: currentUserId -> Set of FriendIds

    fun sendInvitation(context: Context, targetUserId: String, senderInvitation: Invitation) {
        Log.d(TAG, "Sending invitation to $targetUserId from ${senderInvitation.name} (ID: ${senderInvitation.id})")
        val allInvitations = getAllInvitationsMap(context)
        val userInvitations = allInvitations[targetUserId]?.toMutableList() ?: mutableListOf()
        
        // Remove old invitation from same sender if exists to avoid duplicates
        userInvitations.removeAll { it.id == senderInvitation.id }
        userInvitations.add(senderInvitation)
        
        allInvitations[targetUserId] = userInvitations
        saveAllInvitationsMap(context, allInvitations)
        Log.d(TAG, "Invitation saved. Total for $targetUserId: ${userInvitations.size}")
    }

    fun getInvitationsForUser(context: Context, userId: String): List<Invitation> {
        val allInvs = getAllInvitationsMap(context)
        val invs = allInvs[userId] ?: emptyList()
        Log.d(TAG, "Retrieved ${invs.size} invitations for user $userId")
        return invs
    }

    private fun getAllInvitationsMap(context: Context): MutableMap<String, List<Invitation>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_INVITATIONS, null) ?: return mutableMapOf()
        
        val map = mutableMapOf<String, List<Invitation>>()
        try {
            val root = JSONObject(json)
            val keys = root.keys()
            while (keys.hasNext()) {
                val userId = keys.next()
                val array = root.getJSONArray(userId)
                val list = mutableListOf<Invitation>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(Invitation(
                        id = obj.optString("id", ""),
                        name = obj.optString("name", "Unknown"),
                        title = obj.optString("title", ""),
                        reason = obj.optString("reason", "")
                    ))
                }
                map[userId] = list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing invitations JSON", e)
        }
        return map
    }

    private fun saveAllInvitationsMap(context: Context, map: Map<String, List<Invitation>>) {
        val root = JSONObject()
        for ((userId, list) in map) {
            val array = JSONArray()
            for (inv in list) {
                val obj = JSONObject()
                obj.put("id", inv.id)
                obj.put("name", inv.name)
                obj.put("title", inv.title)
                obj.put("reason", inv.reason)
                array.put(obj)
            }
            root.put(userId, array)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_INVITATIONS, root.toString())
            .apply()
    }

    fun removeInvitation(context: Context, currentUserId: String, senderId: String) {
        val allInvs = getAllInvitationsMap(context)
        val userInvs = allInvs[currentUserId]?.toMutableList() ?: return
        userInvs.removeAll { it.id == senderId }
        allInvs[currentUserId] = userInvs
        saveAllInvitationsMap(context, allInvs)
    }

    fun acceptConnection(context: Context, currentUserId: String, friendId: String) {
        addFriend(context, currentUserId, friendId)
        addFriend(context, friendId, currentUserId)
    }

    private fun addFriend(context: Context, userId: String, friendId: String) {
        val allFriends = getAllFriendsMap(context)
        val friends = allFriends[userId]?.toMutableSet() ?: mutableSetOf()
        friends.add(friendId)
        allFriends[userId] = friends
        saveAllFriendsMap(context, allFriends)
    }

    fun getFriendsForUser(context: Context, userId: String): Set<String> {
        return getAllFriendsMap(context)[userId] ?: emptySet()
    }

    private fun getAllFriendsMap(context: Context): MutableMap<String, Set<String>> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FRIENDS, null) ?: return mutableMapOf()
        
        val map = mutableMapOf<String, Set<String>>()
        try {
            val root = JSONObject(json)
            val keys = root.keys()
            while (keys.hasNext()) {
                val userId = keys.next()
                val array = root.getJSONArray(userId)
                val set = mutableSetOf<String>()
                for (i in 0 until array.length()) set.add(array.getString(i))
                map[userId] = set
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing friends JSON", e)
        }
        return map
    }

    private fun saveAllFriendsMap(context: Context, map: Map<String, Set<String>>) {
        val root = JSONObject()
        for ((userId, set) in map) {
            root.put(userId, JSONArray(set))
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FRIENDS, root.toString())
            .apply()
    }

    fun clearAllData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

package com.example.skillxchange

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ChatCache {

    private fun chatKey(currentUserId: String, otherUserId: String): String {
        val sortedIds = listOf(currentUserId, otherUserId).sorted()
        return "chat_${sortedIds[0]}_${sortedIds[1]}"
    }

    private fun viewedKey(myId: String, otherId: String): String {
        return "viewed_${myId}_with_${otherId}"
    }

    private const val PREFS_VIEWED = "chat_viewed_status"

    fun save(context: Context, currentUserId: String, otherUserId: String, messages: List<ChatMessage>) {
        val array = JSONArray()
        for (msg in messages) {
            val obj = JSONObject()
            obj.put("text", msg.text)
            obj.put("senderId", msg.senderId)
            obj.put("timestamp", msg.timestamp)
            if (msg.sharedPostId != null) {
                obj.put("sharedPostId", msg.sharedPostId)
            }
            array.put(obj)
        }
        context.getSharedPreferences("chat_cache", Context.MODE_PRIVATE)
            .edit()
            .putString(chatKey(currentUserId, otherUserId), array.toString())
            .apply()

        // When saving a message, mark it as unviewed for the RECIPIENT
        // In this local mock, if 'currentUserId' is sending, 'otherUserId' is receiving.
        if (messages.isNotEmpty() && messages.last().senderId == currentUserId) {
            markViewed(context, otherUserId, currentUserId, false)
        }
    }

    fun load(context: Context, currentUserId: String, otherUserId: String): MutableList<ChatMessage> {
        val prefs = context.getSharedPreferences("chat_cache", Context.MODE_PRIVATE)
        val json = prefs.getString(chatKey(currentUserId, otherUserId), null) ?: return mutableListOf()
        val list = mutableListOf<ChatMessage>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChatMessage(
                        text = obj.getString("text"),
                        senderId = obj.optString("senderId", ""),
                        timestamp = obj.getLong("timestamp"),
                        sharedPostId = if (obj.has("sharedPostId")) obj.getString("sharedPostId") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun markViewed(context: Context, myId: String, otherId: String, viewed: Boolean) {
        context.getSharedPreferences(PREFS_VIEWED, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(viewedKey(myId, otherId), viewed)
            .apply()
    }

    fun isViewed(context: Context, myId: String, otherId: String): Boolean {
        return context.getSharedPreferences(PREFS_VIEWED, Context.MODE_PRIVATE)
            .getBoolean(viewedKey(myId, otherId), true)
    }
    
    fun hasAnyUnviewed(context: Context, myId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_VIEWED, Context.MODE_PRIVATE)
        val all = prefs.all
        val prefix = "viewed_${myId}_with_"
        for (entry in all) {
            if (entry.key.startsWith(prefix) && entry.value == false) return true
        }
        return false
    }
}

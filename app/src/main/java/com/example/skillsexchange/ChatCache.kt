package com.example.skillsexchange

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ChatCache {

    private fun key(userId: String) = "chat_$userId"

    fun save(context: Context, userId: String, messages: List<ChatMessage>) {
        val array = JSONArray()
        for (msg in messages) {
            val obj = JSONObject()
            obj.put("text", msg.text)
            obj.put("isSentByMe", msg.isSentByMe)
            obj.put("timestamp", msg.timestamp)
            if (msg.sharedPostId != null) {
                obj.put("sharedPostId", msg.sharedPostId)
            }
            array.put(obj)
        }
        context.getSharedPreferences("chat_cache", Context.MODE_PRIVATE)
            .edit()
            .putString(key(userId), array.toString())
            .apply()
    }

    fun load(context: Context, userId: String): MutableList<ChatMessage> {
        val prefs = context.getSharedPreferences("chat_cache", Context.MODE_PRIVATE)
        val json = prefs.getString(key(userId), null) ?: return mutableListOf()
        val list = mutableListOf<ChatMessage>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ChatMessage(
                    text = obj.getString("text"),
                    isSentByMe = obj.getBoolean("isSentByMe"),
                    timestamp = obj.getLong("timestamp"),
                    sharedPostId = if (obj.has("sharedPostId")) obj.getString("sharedPostId") else null
                )
            )
        }
        return list
    }
}
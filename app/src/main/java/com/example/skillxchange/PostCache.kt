package com.example.skillxchange

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PostCache {
    private const val PREFS_NAME = "post_cache"
    private const val KEY_POSTS = "all_posts"

    fun savePost(context: Context, post: Post) {
        val posts = getAllPosts(context).toMutableList()
        posts.add(0, post) // Add to top
        persistPosts(context, posts)
    }

    fun getAllPosts(context: Context): List<Post> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_POSTS, null) ?: return emptyList()
        val list = mutableListOf<Post>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Post(
                        id = obj.optString("id", ""),
                        userId = obj.optString("userId", ""),
                        userName = obj.optString("userName", ""),
                        userTitle = obj.optString("userTitle", ""),
                        content = obj.optString("content", ""),
                        timestamp = obj.optString("timestamp", ""),
                        likes = obj.optInt("likes", 0),
                        comments = obj.optInt("comments", 0),
                        hasVideo = obj.optBoolean("hasVideo", false),
                        imageUri = if (obj.has("imageUri")) obj.getString("imageUri") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun persistPosts(context: Context, posts: List<Post>) {
        val array = JSONArray()
        for (p in posts) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("userId", p.userId)
            obj.put("userName", p.userName)
            obj.put("userTitle", p.userTitle)
            obj.put("content", p.content)
            obj.put("timestamp", p.timestamp)
            obj.put("likes", p.likes)
            obj.put("comments", p.comments)
            obj.put("hasVideo", p.hasVideo)
            if (p.imageUri != null) {
                obj.put("imageUri", p.imageUri)
            }
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_POSTS, array.toString())
            .apply()
    }
}

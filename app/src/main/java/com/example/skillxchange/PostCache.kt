package com.example.skillxchange

import android.content.Context
import com.google.firebase.Timestamp
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
                
                val seconds = obj.optLong("timestamp_seconds", -1L)
                val nanos = obj.optInt("timestamp_nanos", 0)
                val timestamp = if (seconds != -1L) Timestamp(seconds, nanos) else null

                // Parse likedBy list
                val likedBy = mutableListOf<String>()
                obj.optJSONArray("likedBy")?.let { likedArray ->
                    for (j in 0 until likedArray.length()) {
                        likedBy.add(likedArray.getString(j))
                    }
                }

                // Parse tags list
                val tags = mutableListOf<String>()
                obj.optJSONArray("tags")?.let { tagsArray ->
                    for (j in 0 until tagsArray.length()) {
                        tags.add(tagsArray.getString(j))
                    }
                }

                list.add(
                    Post(
                        id = obj.optString("id", ""),
                        userId = obj.optString("userId", ""),
                        userName = obj.optString("userName", ""),
                        userTitle = obj.optString("userTitle", ""),
                        userPhotoUrl = obj.optString("userPhotoUrl", ""),
                        content = obj.optString("content", ""),
                        imageUrl = if (obj.has("imageUrl") && !obj.isNull("imageUrl")) obj.getString("imageUrl") else null,
                        timestamp = timestamp,
                        likedBy = likedBy,
                        commentsCount = obj.optInt("commentsCount", 0),
                        tags = tags
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
            obj.put("userPhotoUrl", p.userPhotoUrl)
            obj.put("content", p.content)
            obj.put("imageUrl", p.imageUrl ?: JSONObject.NULL)
            if (p.timestamp != null) {
                obj.put("timestamp_seconds", p.timestamp.seconds)
                obj.put("timestamp_nanos", p.timestamp.nanoseconds)
            }
            
            // Serialize lists
            obj.put("likedBy", JSONArray(p.likedBy))
            obj.put("tags", JSONArray(p.tags))
            obj.put("commentsCount", p.commentsCount)

            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_POSTS, array.toString())
            .apply()
    }
}

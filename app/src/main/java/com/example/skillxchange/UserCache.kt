package com.example.skillxchange

import android.content.Context
import com.example.skillxchange.model.User
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local cache for user profiles.
 * Used for storing temporary data or providing mock data for tests.
 */
object UserCache {
    private const val PREFS_NAME = "user_storage"
    private const val KEY_USERS = "registered_users"

    private val defaultUsers = listOf(
        User(uid = "user_1", name = "Ali Hassan", tagline = "Kotlin Developer", bio = "Loves Android", teachSkills = listOf("Kotlin", "Android"), learnSkills = listOf("UI Design", "Figma")),
        User(uid = "user_2", name = "Sara Khan", tagline = "UI Designer", bio = "Creative designer", teachSkills = listOf("Figma", "UI Design"), learnSkills = listOf("Kotlin", "Firebase")),
        User(uid = "user_3", name = "Ahmed Raza", tagline = "Backend Dev", bio = "Firebase expert", teachSkills = listOf("Firebase", "Node.js"), learnSkills = listOf("Android", "Kotlin")),
        User(uid = "user_4", name = "Zara Ahmed", tagline = "Data Analyst", bio = "Python enthusiast", teachSkills = listOf("Python", "Excel"), learnSkills = listOf("Android", "UI Design")),
        User(uid = "user_5", name = "Usman Ali", tagline = "Public Speaker", bio = "Communication coach", teachSkills = listOf("Communication", "Leadership"), learnSkills = listOf("Python", "Firebase")),
        User(uid = "user_6", name = "Hina Shah", tagline = "Web Developer", bio = "React developer", teachSkills = listOf("React", "JavaScript"), learnSkills = listOf("Kotlin", "Android")),
        User(uid = "user_7", name = "John Lee", tagline = "Full Stack Developer", bio = "Node.js & React", teachSkills = listOf("React"), learnSkills = listOf("DevOps"))
    )

    fun saveUser(context: Context, user: User) {
        val users = getAllUsers(context).toMutableList()
        val index = users.indexOfFirst { it.uid == user.uid }
        if (index != -1) {
            users[index] = user
        } else {
            users.add(user)
        }
        persistUsers(context, users)
    }

    private fun persistUsers(context: Context, users: List<User>) {
        val array = JSONArray()
        for (u in users) {
            val obj = JSONObject()
            obj.put("uid", u.uid)
            obj.put("name", u.name)
            obj.put("tagline", u.tagline)
            obj.put("bio", u.bio)
            obj.put("teachSkills", JSONArray(u.teachSkills))
            obj.put("learnSkills", JSONArray(u.learnSkills))
            obj.put("rating", u.rating)
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USERS, array.toString())
            .apply()
    }

    fun getAllUsers(context: Context): List<User> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_USERS, null)
        
        if (json == null) {
            // First time: save default users
            persistUsers(context, defaultUsers)
            return defaultUsers
        }

        val list = mutableListOf<User>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val teachArr = obj.optJSONArray("teachSkills")
                val learnArr = obj.optJSONArray("learnSkills")
                val teachList = mutableListOf<String>()
                val learnList = mutableListOf<String>()
                
                teachArr?.let {
                    for (j in 0 until it.length()) teachList.add(it.getString(j))
                }
                learnArr?.let {
                    for (j in 0 until it.length()) learnList.add(it.getString(j))
                }

                list.add(User(
                    uid = obj.optString("uid", ""),
                    name = obj.optString("name", "Unknown"),
                    tagline = obj.optString("tagline", ""),
                    bio = obj.optString("bio", ""),
                    teachSkills = teachList,
                    learnSkills = learnList,
                    rating = obj.optDouble("rating", 4.5)
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun clearData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

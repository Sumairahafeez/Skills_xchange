package com.example.skillxchange

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object UserCache {
    private const val PREFS_NAME = "user_storage"
    private const val KEY_USERS = "registered_users"

    private val defaultUsers = listOf(
        User("1", "Ali Hassan", "Kotlin Developer", "Loves Android", listOf("Kotlin", "Android"), listOf("UI Design", "Figma")),
        User("2", "Sara Khan", "UI Designer", "Creative designer", listOf("Figma", "UI Design"), listOf("Kotlin", "Firebase")),
        User("3", "Ahmed Raza", "Backend Dev", "Firebase expert", listOf("Firebase", "Node.js"), listOf("Android", "Kotlin")),
        User("4", "Zara Ahmed", "Data Analyst", "Python enthusiast", listOf("Python", "Excel"), listOf("Android", "UI Design")),
        User("5", "Usman Ali", "Public Speaker", "Communication coach", listOf("Communication", "Leadership"), listOf("Python", "Firebase")),
        User("6", "Hina Shah", "Web Developer", "React developer", listOf("React", "JavaScript"), listOf("Kotlin", "Android")),
        User("7", "John Lee", "Full Stack Developer", "Node.js & React", listOf("React"), listOf("DevOps"))
    )

    fun saveUser(context: Context, user: User) {
        val users = getAllUsers(context).toMutableList()
        if (users.none { it.id == user.id }) {
            users.add(user)
            persistUsers(context, users)
        }
    }

    private fun persistUsers(context: Context, users: List<User>) {
        val array = JSONArray()
        for (u in users) {
            val obj = JSONObject()
            obj.put("id", u.id)
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
                    id = obj.optString("id", ""),
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

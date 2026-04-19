package com.example.skillxchange

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object CredentialCache {
    private const val PREFS_NAME = "credential_storage"
    private const val KEY_CREDENTIALS = "user_credentials"

    data class Credential(val userId: String, val email: String, val password: String, val name: String)

    fun saveCredential(context: Context, cred: Credential) {
        val creds = getAllCredentials(context).toMutableList()
        if (creds.none { it.email == cred.email }) {
            creds.add(cred)
            persistCredentials(context, creds)
        }
    }

    private fun persistCredentials(context: Context, creds: List<Credential>) {
        val array = JSONArray()
        for (c in creds) {
            val obj = JSONObject()
            obj.put("userId", c.userId)
            obj.put("email", c.email)
            obj.put("password", c.password)
            obj.put("name", c.name)
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CREDENTIALS, array.toString())
            .apply()
    }

    fun getAllCredentials(context: Context): List<Credential> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CREDENTIALS, null) ?: return emptyList()
        val list = mutableListOf<Credential>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                // Use optString to avoid JSONException if a key is missing
                list.add(Credential(
                    obj.optString("userId", "unknown"),
                    obj.optString("email", ""),
                    obj.optString("password", ""),
                    obj.optString("name", "")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getCredential(context: Context, email: String): Credential? {
        return getAllCredentials(context).find { it.email == email }
    }

    fun clearData(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

package com.example.skillxchange

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryConfig {
    fun initialize(context: Context) {
        val config = mapOf(
            "cloud_name" to "dqdpcdco3",
            "secure" to true
        )
        try {
            MediaManager.init(context, config)
        } catch (e: Exception) {
            // Already initialized
        }
    }
}

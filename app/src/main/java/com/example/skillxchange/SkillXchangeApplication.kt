package com.example.skillxchange

import android.app.Application

class SkillXchangeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Cloudinary once for the entire app
        CloudinaryConfig.initialize(this)
    }
}

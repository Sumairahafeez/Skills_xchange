package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSession()
        }, 2000)
    }

    private fun checkUserSession() {
        if (auth.currentUser != null) {
            // User is signed in, go to Home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // No user is signed in, go to Login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}

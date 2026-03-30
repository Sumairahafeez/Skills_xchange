package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tilEmail    = findViewById<TextInputLayout>(R.id.tilLoginEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilLoginPassword)
        val etEmail     = findViewById<TextInputEditText>(R.id.etLoginEmail)
        val etPassword  = findViewById<TextInputEditText>(R.id.etLoginPassword)
        val btnLogin    = findViewById<MaterialButton>(R.id.btnLogin)
        val tvSignUp    = findViewById<TextView>(R.id.btnGoToSignup)
        val tvForgot    = findViewById<TextView>(R.id.tvForgotPassword)

        // Show success message if coming from signup
        if (intent.getBooleanExtra("signup_success", false)) {
            Toast.makeText(this, "Account created! Please sign in.", Toast.LENGTH_LONG).show()
        }

        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            var valid    = true

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Enter a valid email address"; valid = false
            } else { tilEmail.error = null }

            if (password.isEmpty()) {
                tilPassword.error = "Enter your password"; valid = false
            } else { tilPassword.error = null }

            if (!valid) return@setOnClickListener

            val prefs         = getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE)
            val savedEmail    = prefs.getString("user_email", null)
            val savedPassword = prefs.getString("user_password", null)
            val savedName     = prefs.getString("user_name", "User")

            when {
                savedEmail == null -> {
                    // No account yet — prompt to sign up
                    tilEmail.error = "No account found. Please sign up first."
                }
                email != savedEmail -> {
                    tilEmail.error = "Email not recognised"
                }
                password != savedPassword -> {
                    tilPassword.error = "Incorrect password"
                }
                else -> {
                    // Credentials match — mark user as logged in
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        tvForgot.setOnClickListener {
            Toast.makeText(this, "Password reset link sent!", Toast.LENGTH_SHORT).show()
        }
    }
}
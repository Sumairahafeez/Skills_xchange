package com.example.skillxchange

import android.content.Context
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

            btnLogin.isEnabled = false
            CredentialCache.getCredential(this, email) { credential ->
                btnLogin.isEnabled = true
                val prefs = getSharedPreferences("skillsxchange_prefs", Context.MODE_PRIVATE)

                when {
                    credential == null -> {
                        tilEmail.error = "No account found with this email."
                    }
                    credential.password != password -> {
                        tilPassword.error = "Incorrect password"
                    }
                    else -> {
                        prefs.edit()
                            .putBoolean("is_logged_in", true)
                            .putString("user_id", credential.userId)
                            .putString("user_name", credential.name)
                            .putString("user_email", credential.email)
                            .apply()
                            
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                }
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        tvForgot.setOnClickListener {
            Toast.makeText(this, "Password reset link sent!", Toast.LENGTH_SHORT).show()
        }
        
        tvForgot.setOnLongClickListener {
            // Note: Firebase clearData usually doesn't make sense for global data,
            // but we clear the local prefs.
            val prefs = getSharedPreferences("skillsxchange_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            Toast.makeText(this, "Local session cleared!", Toast.LENGTH_LONG).show()
            true
        }
    }
}

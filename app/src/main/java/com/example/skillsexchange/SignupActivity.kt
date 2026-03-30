package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val tilName     = findViewById<TextInputLayout>(R.id.tilSignupName)
        val tilEmail    = findViewById<TextInputLayout>(R.id.tilSignupEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilSignupPassword)
        val tilConfirm  = findViewById<TextInputLayout>(R.id.tilSignupConfirmPassword)

        val etName     = findViewById<TextInputEditText>(R.id.etSignupName)
        val etEmail    = findViewById<TextInputEditText>(R.id.etSignupEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etSignupPassword)
        val etConfirm  = findViewById<TextInputEditText>(R.id.etSignupConfirmPassword)

        val btnSignup     = findViewById<MaterialButton>(R.id.btnSignup)
        val tvBackToLogin = findViewById<TextView>(R.id.btnBackToLogin)

        btnSignup.setOnClickListener {
            val name     = etName.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirm  = etConfirm.text.toString()
            var valid    = true

            if (name.isEmpty()) {
                tilName.error = "Enter your full name"; valid = false
            } else { tilName.error = null }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Enter a valid email address"; valid = false
            } else { tilEmail.error = null }

            if (password.length < 6) {
                tilPassword.error = "Password must be at least 6 characters"; valid = false
            } else { tilPassword.error = null }

            if (confirm != password) {
                tilConfirm.error = "Passwords do not match"; valid = false
            } else { tilConfirm.error = null }

            if (valid) {
                // Persist credentials & name in SharedPreferences
                getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("user_name", name)
                    .putString("user_email", email)
                    .putString("user_password", password)
                    .apply()

                // Go to Login, NOT Home
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("signup_success", true)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }

        tvBackToLogin.setOnClickListener { finish() }
    }
}
package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.skillxchange.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var btnSignup: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val tilName = findViewById<TextInputLayout>(R.id.tilSignupName)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilSignupEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilSignupPassword)
        val tilConfirm = findViewById<TextInputLayout>(R.id.tilSignupConfirmPassword)

        val etName = findViewById<TextInputEditText>(R.id.etSignupName)
        val etEmail = findViewById<TextInputEditText>(R.id.etSignupEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etSignupPassword)
        val etConfirm = findViewById<TextInputEditText>(R.id.etSignupConfirmPassword)

        btnSignup = findViewById(R.id.btnSignup)
        val tvBackToLogin = findViewById<TextView>(R.id.btnBackToLogin)

        btnSignup.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirm = etConfirm.text.toString()
            var valid = true

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
                btnSignup.isEnabled = false
                createAccount(name, email, password)
            }
        }

        tvBackToLogin.setOnClickListener { finish() }
    }

    private fun createAccount(name: String, email: String, password: String) {
        // Prevent multiple simultaneous attempts
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    firebaseUser?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            val uid = firebaseUser.uid
                            saveUserToFirestore(uid, name, email)
                        }
                } else {
                    btnSignup.isEnabled = true
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this, "This email is already registered. Please Login instead.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "SIGNUP ERROR: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String) {
        val user = User(
            uid = uid,
            name = name,
            email = email,
            tagline = "New Member",
            photoUrl = "",
            skills = emptyList(),
            connections = emptyList(),
            createdAt = Timestamp.now()
        )

        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                btnSignup.isEnabled = true
                Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

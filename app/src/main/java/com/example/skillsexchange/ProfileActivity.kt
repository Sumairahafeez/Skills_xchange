package com.example.skillsexchange

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var tvProfileName: TextView
    private lateinit var tvTagline: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvProfileName = findViewById(R.id.tvProfileNameDetail)
        tvTagline = findViewById(R.id.tvProfileTagline)
        val toolbar = findViewById<MaterialToolbar>(R.id.profileToolbar)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            showEditProfileDialog()
        }
        
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            finishAffinity() // Mock logout
        }
    }

    private fun showEditProfileDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        bottomSheetDialog.setContentView(view)

        val etName = view.findViewById<TextInputEditText>(R.id.etEditName)
        val etTagline = view.findViewById<TextInputEditText>(R.id.etEditTagline)
        val btnSave = view.findViewById<Button>(R.id.btnSaveProfile)

        // Pre-fill with current data
        etName.setText(tvProfileName.text)
        etTagline.setText(tvTagline.text)

        btnSave.setOnClickListener {
            tvProfileName.text = etName.text.toString()
            tvTagline.text = etTagline.text.toString()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}

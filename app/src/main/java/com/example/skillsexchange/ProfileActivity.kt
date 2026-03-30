package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvProfileName: TextView
    private lateinit var tvTagline: TextView
    private lateinit var chipGroupTeach: ChipGroup
    private lateinit var chipGroupLearn: ChipGroup

    private val teachSkills = mutableListOf("Kotlin", "Android")
    private val learnSkills = mutableListOf("UI Design", "Firebase")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvProfileName  = findViewById(R.id.tvProfileNameDetail)
        tvTagline      = findViewById(R.id.tvProfileTagline)
        chipGroupTeach = findViewById(R.id.chipGroupTeach)
        chipGroupLearn = findViewById(R.id.chipGroupLearn)

        // Load the signed-in user's name from SharedPreferences
        val prefs    = getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Osama") ?: "Osama"
        tvProfileName.text = userName

        findViewById<MaterialToolbar>(R.id.profileToolbar)
            .setNavigationOnClickListener { finish() }

        renderChips(chipGroupTeach, teachSkills, isTeach = true)
        renderChips(chipGroupLearn, learnSkills, isTeach = false)

        findViewById<Button>(R.id.btnEditProfile).setOnClickListener { showEditProfileDialog(prefs) }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // Clear login state and go back to Login screen
            prefs.edit().putBoolean("is_logged_in", false).apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun renderChips(group: ChipGroup, skills: MutableList<String>, isTeach: Boolean) {
        group.removeAllViews()
        for (skill in skills) {
            val chip = Chip(this).apply {
                text = skill
                isCloseIconVisible = true
                setChipBackgroundColorResource(if (isTeach) R.color.color_primary_light else R.color.color_secondary)
                setTextColor(getColor(R.color.white))
                setCloseIconTintResource(R.color.white)
                setOnCloseIconClickListener {
                    skills.remove(skill)
                    renderChips(group, skills, isTeach)
                }
            }
            group.addView(chip)
        }
        val addChip = Chip(this).apply {
            text = "+ Add skill"
            setChipBackgroundColorResource(R.color.color_background_soft)
            setTextColor(getColor(R.color.color_primary))
            setOnClickListener { showAddSkillDialog(skills, group, isTeach) }
        }
        group.addView(addChip)
    }

    private fun showAddSkillDialog(skills: MutableList<String>, group: ChipGroup, isTeach: Boolean) {
        val dialog = BottomSheetDialog(this)
        val view   = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        dialog.setContentView(view)

        val etSkill  = view.findViewById<TextInputEditText>(R.id.etEditName)
        val tilName  = view.findViewById<TextInputLayout>(R.id.tilEditName)
        tilName.hint = "Enter skill name"
        view.findViewById<TextInputLayout>(R.id.tilEditTagline).visibility = View.GONE
        view.findViewById<TextInputLayout>(R.id.tilEditBio).visibility     = View.GONE

        val btnSave = view.findViewById<Button>(R.id.btnSaveProfile)
        btnSave.text = "Add Skill"
        btnSave.setOnClickListener {
            val skill = etSkill.text.toString().trim()
            if (skill.isEmpty()) { Toast.makeText(this, "Please enter a skill", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (skills.contains(skill)) { Toast.makeText(this, "Skill already added", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            skills.add(skill)
            renderChips(group, skills, isTeach)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showEditProfileDialog(prefs: android.content.SharedPreferences) {
        val dialog  = BottomSheetDialog(this)
        val view    = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        dialog.setContentView(view)

        val etName    = view.findViewById<TextInputEditText>(R.id.etEditName)
        val etTagline = view.findViewById<TextInputEditText>(R.id.etEditTagline)
        val btnSave   = view.findViewById<Button>(R.id.btnSaveProfile)

        etName.setText(tvProfileName.text)
        etTagline.setText(tvTagline.text)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) { Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            tvProfileName.text = name
            tvTagline.text     = etTagline.text.toString().trim()
            // Persist updated name
            prefs.edit().putString("user_name", name).apply()
            dialog.dismiss()
        }
        dialog.show()
    }
}
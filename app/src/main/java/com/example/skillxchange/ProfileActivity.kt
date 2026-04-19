package com.example.skillxchange

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvProfileName: TextView
    private lateinit var tvTagline: TextView
    private lateinit var tvProfileBio: TextView
    private lateinit var chipGroupTeach: ChipGroup
    private lateinit var chipGroupLearn: ChipGroup
    private lateinit var ivProfilePicture: ShapeableImageView
    private lateinit var tvPostsCount: TextView

    private var teachSkills = mutableListOf<String>()
    private var learnSkills = mutableListOf<String>()
    
    private var isOwnProfile = true
    private var targetUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvProfileName = findViewById(R.id.tvProfileNameDetail)
        tvTagline = findViewById(R.id.tvProfileTagline)
        tvProfileBio = findViewById(R.id.tvProfileBio)
        chipGroupTeach = findViewById(R.id.chipGroupTeach)
        chipGroupLearn = findViewById(R.id.chipGroupLearn)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        tvPostsCount = findViewById(R.id.tvLessonsCount)

        val prefs = getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE)
        val currentUserId = prefs.getString("user_id", "") ?: ""
        
        targetUserId = intent.getStringExtra("userId")
        isOwnProfile = targetUserId == null || targetUserId == currentUserId

        if (isOwnProfile) {
            setupOwnProfile(prefs, currentUserId)
        } else {
            setupOtherProfile(targetUserId!!, currentUserId)
        }

        findViewById<MaterialToolbar>(R.id.profileToolbar)
            .setNavigationOnClickListener { finish() }

        // Logout logic
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            prefs.edit(commit = true) {
                // We clear everything EXCEPT the "registered_users" in user_storage 
                // but wait, prefs here is "skillsxchange_prefs".
                // UserCache uses "user_storage". So clearing this is fine.
                clear()
            }
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupOwnProfile(prefs: android.content.SharedPreferences, userId: String) {
        // Load from UserCache to be sure
        val user = UserCache.getAllUsers(this).find { it.id == userId }
        
        val userName = user?.name ?: prefs.getString("user_name", "User") ?: "User"
        val userTagline = user?.tagline ?: prefs.getString("user_tagline", "Your tagline here") ?: "Your tagline here"
        val userBio = user?.bio ?: prefs.getString("user_bio", "Add your bio by tapping Edit Profile below.") ?: "Add your bio by tapping Edit Profile below."
        
        tvProfileName.text = userName
        tvTagline.text = userTagline
        tvProfileBio.text = userBio

        teachSkills = user?.teachSkills?.toMutableList() ?: loadSkills(prefs, "teach_skills", mutableListOf("Kotlin", "Android"))
        learnSkills = user?.learnSkills?.toMutableList() ?: loadSkills(prefs, "learn_skills", mutableListOf("UI Design", "Firebase"))

        val savedPicUri = prefs.getString("profile_pic_uri", null)
        if (savedPicUri != null) {
            try { ivProfilePicture.setImageURI(savedPicUri.toUri()) } catch (e: Exception) {}
        }

        renderChips(chipGroupTeach, teachSkills, isTeach = true)
        renderChips(chipGroupLearn, learnSkills, isTeach = false)

        val btnEdit = findViewById<Button>(R.id.btnEditProfile)
        btnEdit.text = "Edit Profile"
        btnEdit.setOnClickListener { showEditProfileDialog(userId) }

        ivProfilePicture.setOnClickListener(null)
        ivProfilePicture.isClickable = false
        
        val allPosts = PostCache.getAllPosts(this)
        val myPosts = allPosts.filter { it.userId == userId }
        tvPostsCount.text = myPosts.size.toString()
        findViewById<TextView>(R.id.tvLessonsLabel).text = "Posts"
    }

    private fun setupOtherProfile(otherId: String, myId: String) {
        val user = UserCache.getAllUsers(this).find { it.id == otherId } ?: return
        
        tvProfileName.text = user.name
        tvTagline.text = user.tagline
        tvProfileBio.text = user.bio
        
        teachSkills = user.teachSkills.toMutableList()
        learnSkills = user.learnSkills.toMutableList()
        
        renderChips(chipGroupTeach, teachSkills, isTeach = true, canEdit = false)
        renderChips(chipGroupLearn, learnSkills, isTeach = false, canEdit = false)
        
        val btnConnect = findViewById<Button>(R.id.btnEditProfile)
        btnConnect.text = "Connect"
        btnConnect.setOnClickListener {
            val myName = getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE).getString("user_name", "Someone") ?: "Someone"
            val inv = Invitation(myId, myName, "Professional", "Wants to connect")
            ConnectionCache.sendInvitation(this, otherId, inv)
            Toast.makeText(this, "Connection request sent to ${user.name}", Toast.LENGTH_SHORT).show()
            btnConnect.isEnabled = false
            btnConnect.text = "Requested"
        }
        
        findViewById<Button>(R.id.btnLogout).visibility = View.GONE
        
        val allPosts = PostCache.getAllPosts(this)
        val userPosts = allPosts.filter { it.userId == otherId }
        tvPostsCount.text = userPosts.size.toString()
        findViewById<TextView>(R.id.tvLessonsLabel).text = "Posts"
    }

    private fun loadSkills(prefs: android.content.SharedPreferences, key: String, default: MutableList<String>): MutableList<String> {
        val json = prefs.getString(key, null) ?: return default
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) list.add(array.getString(i))
        } catch (e: Exception) { return default }
        return list
    }

    private fun renderChips(group: ChipGroup, skills: MutableList<String>, isTeach: Boolean, canEdit: Boolean = true) {
        group.removeAllViews()
        val prefs = getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE)
        val userId = prefs.getString("user_id", "") ?: ""
        
        for (skill in skills) {
            val chip = Chip(this).apply {
                text = skill
                isCloseIconVisible = canEdit
                setChipBackgroundColorResource(if (isTeach) R.color.color_primary_dark else R.color.color_secondary)
                setTextColor(resources.getColor(R.color.white, null))
                setCloseIconTintResource(R.color.white)
                if (canEdit) {
                    setOnCloseIconClickListener {
                        skills.remove(skill)
                        updateSkillsInCache(userId, skills, isTeach)
                        renderChips(group, skills, isTeach, canEdit)
                    }
                }
            }
            group.addView(chip)
        }

        if (canEdit) {
            val addChip = Chip(this).apply {
                text = "+ Add skill"
                setChipBackgroundColorResource(R.color.color_background_soft)
                setTextColor(resources.getColor(R.color.color_primary, null))
                setOnClickListener { showAddSkillDialog(userId, skills, group, isTeach) }
            }
            group.addView(addChip)
        }
    }

    private fun updateSkillsInCache(userId: String, skills: List<String>, isTeach: Boolean) {
        val user = UserCache.getAllUsers(this).find { it.id == userId } ?: return
        val updatedUser = if (isTeach) user.copy(teachSkills = skills) else user.copy(learnSkills = skills)
        UserCache.saveUser(this, updatedUser)
        
        // Also update legacy prefs for now
        val key = if (isTeach) "teach_skills" else "learn_skills"
        getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE).edit().putString(key, JSONArray(skills).toString()).apply()
    }

    private fun showAddSkillDialog(userId: String, skills: MutableList<String>, group: ChipGroup, isTeach: Boolean) {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        dialog.setContentView(view)

        val etSkill = view.findViewById<TextInputEditText>(R.id.etEditName)
        view.findViewById<TextInputLayout>(R.id.tilEditName).hint = "Enter skill name"
        view.findViewById<TextInputLayout>(R.id.tilEditTagline)?.visibility = View.GONE
        view.findViewById<TextInputLayout>(R.id.tilEditBio)?.visibility = View.GONE

        view.findViewById<Button>(R.id.btnSaveProfile).apply {
            text = "Add Skill"
            setOnClickListener {
                val skill = etSkill.text.toString().trim()
                if (skill.isNotEmpty() && !skills.contains(skill)) {
                    skills.add(skill)
                    updateSkillsInCache(userId, skills, isTeach)
                    renderChips(group, skills, isTeach)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun showEditProfileDialog(userId: String) {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        dialog.setContentView(view)

        val etName = view.findViewById<TextInputEditText>(R.id.etEditName)
        val etTagline = view.findViewById<TextInputEditText>(R.id.etEditTagline)
        val etBio = view.findViewById<TextInputEditText>(R.id.etEditBio)

        etName.setText(tvProfileName.text)
        etTagline.setText(tvTagline.text)
        etBio.setText(tvProfileBio.text)

        view.findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            val name = etName.text.toString().trim()
            val tag = etTagline.text.toString().trim()
            val bio = etBio.text.toString().trim()

            if (name.isNotEmpty()) {
                tvProfileName.text = name
                tvTagline.text = tag
                tvProfileBio.text = bio

                // Update Local Cache
                val user = UserCache.getAllUsers(this).find { it.id == userId }
                if (user != null) {
                    val updatedUser = user.copy(name = name, tagline = tag, bio = bio)
                    UserCache.saveUser(this, updatedUser)
                }

                // Update Prefs for session
                getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE).edit {
                    putString("user_name", name)
                    putString("user_tagline", tag)
                    putString("user_bio", bio)
                    apply()
                }
                
                dialog.dismiss()
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }
}

package com.example.skillsexchange

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.UUID

class CreatePostActivity : AppCompatActivity() {

    companion object {
        val sharedPosts: MutableList<Post> = mutableListOf()
    }

    private val selectedTags   = mutableListOf<String>()
    private var selectedPhotoUri: Uri? = null
    private var selectedTagline = ""

    private val taglineOptions = arrayOf(
        "Skills Exchange Member",
        "Kotlin Developer 🚀",
        "UI/UX Designer ✨",
        "Data Scientist 📊",
        "Public Speaker 🎤",
        "Full Stack Developer 💻",
        "Machine Learning Enthusiast 🤖",
        "Mobile App Developer 📱",
        "Open Source Contributor 🌐",
        "Entrepreneur & Mentor 💡"
    )

    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedPhotoUri = uri
                    showPhotoPreview(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        val prefs    = getSharedPreferences("skillsxchange_prefs", MODE_PRIVATE)
        val userName = prefs.getString("user_name", "You") ?: "You"
        val initial  = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "Y"

        findViewById<TextView>(R.id.tvAuthorName).text    = userName
        findViewById<TextView>(R.id.tvAuthorInitial).text = initial

        val toolbar = findViewById<MaterialToolbar>(R.id.createPostToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val tilTitle   = findViewById<TextInputLayout>(R.id.tilPostTitle)
        val etTitle    = findViewById<TextInputEditText>(R.id.etPostTitle)
        val tilContent = findViewById<TextInputLayout>(R.id.tilPostContent)
        val etContent  = findViewById<TextInputEditText>(R.id.etPostContent)
        val chipGroup  = findViewById<ChipGroup>(R.id.chipGroupSkillTags)
        val btnPost    = findViewById<MaterialButton>(R.id.btnSubmitPost)
        val btnPhoto   = findViewById<MaterialButton>(R.id.btnAddPhoto)
        val tvTagline  = findViewById<TextView>(R.id.tvAuthorTagline)

        tvTagline.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Select your tagline")
                .setItems(taglineOptions) { _, which ->
                    selectedTagline = taglineOptions[which]
                    tvTagline.text = selectedTagline
                }
                .show()
        }

        val skillTags = listOf("Kotlin", "Design", "Public Speaking", "Python",
            "UI/UX", "Leadership", "Marketing", "Data Science")
            
        skillTags.forEach { tag ->
            val chip = Chip(this).apply {
                text = tag
                isCheckable = true
                setChipBackgroundColorResource(R.color.color_background_soft)
                setTextColor(getColor(R.color.color_accent))
                chipStrokeWidth = 2f
                setChipStrokeColorResource(R.color.color_primary_light)
            }
            
            chip.setOnCheckedChangeListener { _, isChecked ->
                // Animation & Color change on click
                if (isChecked) {
                    selectedTags.add(tag)
                    chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.color_primary))
                    chip.setTextColor(getColor(R.color.white))
                    chip.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
                } else {
                    selectedTags.remove(tag)
                    chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.color_background_soft))
                    chip.setTextColor(getColor(R.color.color_accent))
                }
            }
            chipGroup.addView(chip)
        }

        btnPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            photoPickerLauncher.launch(intent)
        }

        findViewById<View>(R.id.fabRemovePhoto).setOnClickListener {
            selectedPhotoUri = null
            hidePhotoPreview()
        }

        btnPost.setOnClickListener {
            val title   = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()
            var valid   = true

            if (title.isEmpty()) { tilTitle.error = "Please add a title"; valid = false }
            else { tilTitle.error = null }

            if (content.isEmpty()) { tilContent.error = "Please write something to share"; valid = false }
            else { tilContent.error = null }

            if (valid) {
                val tagSuffix  = if (selectedTags.isNotEmpty()) "\n\nTags: ${selectedTags.joinToString(", ")}" else ""
                val userTitle  = selectedTagline.ifEmpty { "Skills Exchange Member" }
                val newPost    = Post(
                    id        = UUID.randomUUID().toString(),
                    userName  = userName,
                    userTitle = userTitle,
                    content   = "$title\n\n$content$tagSuffix",
                    timestamp = "Just now",
                    likes     = 0,
                    comments  = 0,
                    hasVideo  = false
                )
                sharedPosts.add(0, newPost)
                Toast.makeText(this, "Your skill post is live! 🎉", Toast.LENGTH_LONG).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun showPhotoPreview(uri: Uri) {
        val card = findViewById<MaterialCardView>(R.id.cardPhotoPreview)
        val iv   = findViewById<android.widget.ImageView>(R.id.ivPhotoPreview)
        iv.setImageURI(uri)
        card.visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnAddPhoto).text = "📷  Change Photo"
    }

    private fun hidePhotoPreview() {
        findViewById<MaterialCardView>(R.id.cardPhotoPreview).visibility = View.GONE
        findViewById<MaterialButton>(R.id.btnAddPhoto).text = "📷  Add Photo"
    }
}
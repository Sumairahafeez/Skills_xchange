package com.example.skillsexchange

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
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
    private var selectedVideoUri: Uri? = null
    private var selectedTagline = ""

    // Tagline options
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

    // Photo picker
    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedPhotoUri = uri
                    selectedVideoUri = null          // only one media type at a time
                    showPhotoPreview(uri)
                    hideVideoPreview()
                }
            }
        }

    // Video picker
    private val videoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedVideoUri = uri
                    selectedPhotoUri = null
                    showVideoPreview(uri)
                    hidePhotoPreview()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        // Read user name from prefs
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
        val btnVideo   = findViewById<MaterialButton>(R.id.btnAddVideo)
        val tvTagline  = findViewById<TextView>(R.id.tvAuthorTagline)

        // ── Tagline selector ──────────────────────────────────────────────
        tvTagline.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Select your tagline")
                .setItems(taglineOptions) { _, which ->
                    selectedTagline = taglineOptions[which]
                    tvTagline.text = selectedTagline
                }
                .show()
        }

        // ── Skill chips ───────────────────────────────────────────────────
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
                if (isChecked) selectedTags.add(tag) else selectedTags.remove(tag)
            }
            chipGroup.addView(chip)
        }

        // ── Photo picker ──────────────────────────────────────────────────
        btnPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            photoPickerLauncher.launch(intent)
        }

        // Remove photo
        findViewById<View>(R.id.fabRemovePhoto).setOnClickListener {
            selectedPhotoUri = null
            hidePhotoPreview()
        }

        // ── Video picker ──────────────────────────────────────────────────
        btnVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            videoPickerLauncher.launch(intent)
        }

        // Remove video
        findViewById<View>(R.id.ivRemoveVideo).setOnClickListener {
            selectedVideoUri = null
            hideVideoPreview()
        }

        // ── Submit ────────────────────────────────────────────────────────
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
                    hasVideo  = selectedVideoUri != null
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
        // Update button label
        findViewById<MaterialButton>(R.id.btnAddPhoto).text = "📷  Change Photo"
    }

    private fun hidePhotoPreview() {
        findViewById<MaterialCardView>(R.id.cardPhotoPreview).visibility = View.GONE
        findViewById<MaterialButton>(R.id.btnAddPhoto).text = "📷  Add Photo"
    }

    private fun showVideoPreview(uri: Uri) {
        val card    = findViewById<MaterialCardView>(R.id.cardVideoPreview)
        val tvName  = findViewById<TextView>(R.id.tvVideoName)
        // Resolve file name from URI
        var fileName = "video_selected.mp4"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && idx >= 0) fileName = cursor.getString(idx)
        }
        tvName.text = fileName
        card.visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnAddVideo).text = "🎬  Change Video"
    }

    private fun hideVideoPreview() {
        findViewById<MaterialCardView>(R.id.cardVideoPreview).visibility = View.GONE
        findViewById<MaterialButton>(R.id.btnAddVideo).text = "🎬  Add Video"
    }
}
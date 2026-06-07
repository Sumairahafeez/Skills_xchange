package com.example.skillxchange

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.skillxchange.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class CreatePostActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var selectedImageUri: Uri? = null
    private var currentUser: User? = null
    private val selectedTags = mutableListOf<String>()

    private lateinit var ivPhotoPreview: android.widget.ImageView
    private lateinit var cardPhotoPreview: MaterialCardView
    private lateinit var btnAddPhoto: MaterialButton
    private lateinit var chipGroupSkillTags: ChipGroup
    private lateinit var btnSubmit: MaterialButton

    private val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            ivPhotoPreview.setImageURI(uri)
            cardPhotoPreview.visibility = View.VISIBLE
            btnAddPhoto.text = "Change Photo"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        CloudinaryConfig.initialize(this)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.createPostToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        ivPhotoPreview = findViewById(R.id.ivPhotoPreview)
        cardPhotoPreview = findViewById(R.id.cardPhotoPreview)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        chipGroupSkillTags = findViewById(R.id.chipGroupSkillTags)
        val etContent = findViewById<TextInputEditText>(R.id.etPostContent)
        btnSubmit = findViewById(R.id.btnSubmitPost)
        val fabRemovePhoto = findViewById<View>(R.id.fabRemovePhoto)

        fetchUserProfile()
        setupTagSuggestions()

        btnAddPhoto.setOnClickListener {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        fabRemovePhoto.setOnClickListener {
            selectedImageUri = null
            cardPhotoPreview.visibility = View.GONE
            btnAddPhoto.text = "Add Photo"
        }

        btnSubmit.setOnClickListener {
            val content = etContent.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "Please enter some content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            btnSubmit.isEnabled = false
            uploadPost(content)
        }
    }

    private fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                currentUser = document.toObject(User::class.java)
                findViewById<TextView>(R.id.tvAuthorName).text = currentUser?.name ?: auth.currentUser?.displayName ?: "User"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                findViewById<TextView>(R.id.tvAuthorName).text = auth.currentUser?.displayName ?: "User"
            }
    }

    private fun setupTagSuggestions() {
        val commonSkills = listOf("Kotlin", "Android", "Firebase", "UI Design", "Figma", "Python", "React")
        for (skill in commonSkills) {
            val chip = Chip(this).apply {
                text = skill
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedTags.add(skill) else selectedTags.remove(skill)
                }
            }
            chipGroupSkillTags.addView(chip)
        }
    }

    private fun uploadPost(content: String) {
        val postId = UUID.randomUUID().toString()
        val uid = auth.currentUser?.uid ?: return

        if (selectedImageUri != null) {
            MediaManager.get().upload(selectedImageUri)
                .unsigned("ml_default")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val imageUrl = resultData["secure_url"] as String
                        savePostToFirestore(postId, uid, content, imageUrl)
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(this@CreatePostActivity, "Upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                        btnSubmit.isEnabled = true
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                }).dispatch()
        } else {
            savePostToFirestore(postId, uid, content, null)
        }
    }

    private fun savePostToFirestore(postId: String, uid: String, content: String, imageUrl: String?) {
        val post = Post(
            id = postId,
            userId = uid,
            userName = currentUser?.name ?: auth.currentUser?.displayName ?: "Unknown",
            userTitle = currentUser?.tagline ?: "Member",
            userPhotoUrl = currentUser?.photoUrl ?: "",
            content = content,
            imageUrl = imageUrl,
            likedBy = emptyList(),
            commentsCount = 0,
            tags = selectedTags
        )

        db.collection("posts").document(postId)
            .set(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save post: ${it.message}", Toast.LENGTH_SHORT).show()
                btnSubmit.isEnabled = true
            }
    }
}
